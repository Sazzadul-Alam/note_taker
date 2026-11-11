package com.example.notes.dto;

import java.util.List;

public class InterestGroupResponse {
    private String interest;
    private long count;
    private List<UserSummary> users;

    public InterestGroupResponse() {
    }

    public InterestGroupResponse(String interest, long count, List<UserSummary> users) {
        this.interest = interest;
        this.count = count;
        this.users = users;
    }

    public String getInterest() { return interest; }
    public void setInterest(String interest) { this.interest = interest; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
    public List<UserSummary> getUsers() { return users; }
    public void setUsers(List<UserSummary> users) { this.users = users; }

    public static class UserSummary {
        private String id;
        private String name;

        public UserSummary() {}
        public UserSummary(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
    }
}

