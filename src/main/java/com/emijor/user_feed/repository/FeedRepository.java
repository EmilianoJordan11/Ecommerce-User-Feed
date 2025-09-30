package com.emijor.user_feed.repository;


import com.emijor.user_feed.entities.Feed;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<Feed, Long> {

}
