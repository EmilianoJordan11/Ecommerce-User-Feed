package com.emijor.user_feed.services;

import com.emijor.user_feed.dto.CreateFeedDTO;
import com.emijor.user_feed.entities.Feed;
import com.emijor.user_feed.repository.FeedRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class FeedService {

    @Autowired
    private FeedRepository feedRepository;

    public Feed createFeed(CreateFeedDTO dto){
        Feed feed = new Feed(dto);

        return feed;
    }

}
