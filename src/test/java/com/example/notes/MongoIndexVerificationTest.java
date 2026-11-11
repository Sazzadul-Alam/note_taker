package com.example.notes;

import com.example.notes.model.Note;
import com.example.notes.model.Post;
import com.example.notes.model.User;
import com.example.notes.repository.NoteRepository;
import com.example.notes.repository.PostRepository;
import com.example.notes.repository.UserRepository;
import org.bson.Document;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;

@SpringBootTest
public class MongoIndexVerificationTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private PostRepository postRepository;

    @Test
    public void verifyIndexesAndExplainPlans() {
        System.out.println("=== 1. VERIFYING USER INDEXES ===");
        List<IndexInfo> userIndexes = mongoTemplate.indexOps(User.class).getIndexInfo();
        for (IndexInfo info : userIndexes) {
            System.out.println("User Index: " + info.getName() + " -> " + info.getIndexFields());
        }

        System.out.println("\n=== 2. VERIFYING NOTE INDEXES ===");
        List<IndexInfo> noteIndexes = mongoTemplate.indexOps(Note.class).getIndexInfo();
        for (IndexInfo info : noteIndexes) {
            System.out.println("Note Index: " + info.getName() + " -> " + info.getIndexFields());
        }

        System.out.println("\n=== 3. VERIFYING POST INDEXES ===");
        List<IndexInfo> postIndexes = mongoTemplate.indexOps(Post.class).getIndexInfo();
        for (IndexInfo info : postIndexes) {
            System.out.println("Post Index: " + info.getName() + " -> " + info.getIndexFields());
        }

        System.out.println("\n=== 4. EXPLAIN: Login Lookup (User email) ===");
        Query loginQuery = new Query(Criteria.where("email").is("test@example.com"));
        Document loginExplain = mongoTemplate.getCollection("users").find(loginQuery.getQueryObject()).explain();
        System.out.println("Login explain queryPlanner: " + loginExplain.get("queryPlanner"));

        System.out.println("\n=== 5. EXPLAIN: User's Paginated Notes List (ownerId + _id desc) ===");
        Query notesQuery = new Query(Criteria.where("ownerId").is("user123"))
                .with(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "_id")));
        Document notesExplain = mongoTemplate.getCollection("notes").find(notesQuery.getQueryObject())
                .sort(new Document("_id", -1)).explain();
        System.out.println("Notes explain queryPlanner: " + notesExplain.get("queryPlanner"));

        System.out.println("\n=== 6. EXPLAIN: Admin List Users (_id desc) ===");
        Document adminUsersExplain = mongoTemplate.getCollection("users").find()
                .sort(new Document("_id", -1)).explain();
        System.out.println("Admin users explain queryPlanner: " + adminUsersExplain.get("queryPlanner"));

        System.out.println("\n=== 7. EXPLAIN: Admin List All Notes (_id desc) ===");
        Document adminNotesExplain = mongoTemplate.getCollection("notes").find()
                .sort(new Document("_id", -1)).explain();
        System.out.println("Admin notes explain queryPlanner: " + adminNotesExplain.get("queryPlanner"));

        System.out.println("\n=== 8. EXPLAIN: Aggregation 1 - Group by interests ===");
        Aggregation interestsAgg = newAggregation(
                unwind("interests"),
                group("interests")
                        .first("interests").as("interest")
                        .count().as("count")
                        .push(new Document("id", "$_id").append("name", "$name")).as("users"),
                sort(Sort.Direction.ASC, "interest"),
                skip(0),
                limit(10)
        );
        List<Document> pipeline1 = interestsAgg.toPipeline(Aggregation.DEFAULT_CONTEXT);
        Document agg1Explain = mongoTemplate.getDb().runCommand(new Document("explain",
                new Document("aggregate", "users")
                        .append("pipeline", pipeline1)
                        .append("cursor", new Document())
        ).append("verbosity", "queryPlanner"));
        System.out.println("Interests aggregation explain stages: " + (agg1Explain.containsKey("stages") ? agg1Explain.get("stages") : agg1Explain.get("queryPlanner")));

        System.out.println("\n=== 9. EXPLAIN: Aggregation 2 - User posts with $lookup ===");
        AggregationOperation lookupOp = context -> new Document("$lookup",
                new Document("from", "posts")
                        .append("let", new Document("userId", "$_id"))
                        .append("pipeline", List.of(
                                new Document("$match", new Document("$expr",
                                        new Document("$eq", List.of("$authorId", "$$userId"))
                                )),
                                new Document("$sort", new Document("_id", -1)),
                                new Document("$skip", 0),
                                new Document("$limit", 10)
                        ))
                        .append("as", "posts")
        );
        Aggregation userPostsAgg = newAggregation(
                match(Criteria.where("_id").is("sampleId")),
                lookupOp
        );
        List<Document> pipeline2 = userPostsAgg.toPipeline(Aggregation.DEFAULT_CONTEXT);
        Document agg2Explain = mongoTemplate.getDb().runCommand(new Document("explain",
                new Document("aggregate", "users")
                        .append("pipeline", pipeline2)
                        .append("cursor", new Document())
        ).append("verbosity", "queryPlanner"));
        System.out.println("User posts aggregation explain stages: " + (agg2Explain.containsKey("stages") ? agg2Explain.get("stages") : agg2Explain.get("queryPlanner")));

        assertNotNull(userIndexes);
        assertNotNull(noteIndexes);
        assertNotNull(postIndexes);
    }
}
