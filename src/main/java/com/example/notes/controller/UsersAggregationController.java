package com.example.notes.controller;

import com.example.notes.dto.InterestGroupResponse;
import com.example.notes.dto.UserPostsResponse;
import com.example.notes.model.Post;
import org.bson.Document;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@RestController
@RequestMapping("/api/users")
public class UsersAggregationController {

    private final MongoTemplate mongoTemplate;

    public UsersAggregationController(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    /**
     * Group by interests
     * GET /api/users/by-interests?page=&size=
     * - Use mongoTemplate.aggregate() exactly once. No other query methods.
     * - Pipeline: $unwind interests -> $group by interest (collect user id/name, count) -> $sort -> $skip -> $limit
     */
    @GetMapping("/by-interests")
    public ResponseEntity<List<InterestGroupResponse>> getByInterests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        long skip = (long) page * size;

        Aggregation aggregation = newAggregation(
                unwind("interests"),
                group("interests")
                        .first("interests").as("interest")
                        .count().as("count")
                        .push(new Document("id", "$_id").append("name", "$name")).as("users"),
                sort(Sort.Direction.ASC, "interest"),
                skip(skip),
                limit(size)
        );

        AggregationResults<InterestGroupResponse> results = mongoTemplate.aggregate(
                aggregation,
                "users",
                InterestGroupResponse.class
        );

        return ResponseEntity.ok(results.getMappedResults());
    }

    /**
     * User's posts
     * GET /api/users/{id}/posts?page=&size=
     * - Single pipeline on users:
     *   $match _id -> $lookup from posts using let + pipeline ($match $expr $eq authorId, $sort _id:-1, $skip, $limit)
     * - Single aggregation with a $lookup stage.
     */
    @GetMapping("/{id}/posts")
    public ResponseEntity<UserPostsResponse> getUserPosts(
            @PathVariable String id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        long skip = (long) page * size;

        // Construct raw pipeline stage for $lookup with let and internal pipeline
        // let: { userId: "$_id" }
        // pipeline: [
        //   { $match: { $expr: { $eq: [ "$authorId", "$$userId" ] } } },
        //   { $sort: { _id: -1 } },
        //   { $skip: skip },
        //   { $limit: size }
        // ]
        AggregationOperation lookupOperation = context -> new Document("$lookup",
                new Document("from", "posts")
                        .append("let", new Document("userId", "$_id"))
                        .append("pipeline", List.of(
                                new Document("$match", new Document("$expr",
                                        new Document("$eq", List.of("$authorId", "$$userId"))
                                )),
                                new Document("$sort", new Document("_id", -1)),
                                new Document("$skip", skip),
                                new Document("$limit", size)
                        ))
                        .append("as", "posts")
        );

        Aggregation aggregation = newAggregation(
                match(Criteria.where("_id").is(id)),
                lookupOperation,
                project("posts")
                        .and("_id").as("userId")
                        .and("name").as("userName")
                        .and("email").as("email")
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, "users", Document.class);
        Document userDoc = results.getUniqueMappedResult();

        if (userDoc == null) {
            return ResponseEntity.notFound().build();
        }

        List<Document> postDocs = userDoc.getList("posts", Document.class, new ArrayList<>());
        List<Post> posts = new ArrayList<>();
        for (Document doc : postDocs) {
            Post p = new Post();
            p.setId(doc.getString("_id"));
            p.setAuthorId(doc.getString("authorId"));
            p.setTitle(doc.getString("title"));
            p.setContent(doc.getString("content"));
            Object dateObj = doc.get("createdAt");
            if (dateObj instanceof java.util.Date d) {
                p.setCreatedAt(d.toInstant());
            } else if (dateObj instanceof java.time.Instant i) {
                p.setCreatedAt(i);
            }
            posts.add(p);
        }

        UserPostsResponse response = new UserPostsResponse(
                userDoc.getString("userId"),
                userDoc.getString("userName"),
                userDoc.getString("email"),
                posts,
                posts.size(),
                page,
                size
        );

        return ResponseEntity.ok(response);
    }
}

