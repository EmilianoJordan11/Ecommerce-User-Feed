package com.emijor.user_feed.controller;

import com.emijor.user_feed.dto.CreateFeedDTO;
import com.emijor.user_feed.entities.Feed;
import com.emijor.user_feed.services.FeedService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/feed")
public class FeedController {

    @Autowired
    private FeedService feedService;

    @PostMapping
    @Transactional
    public ResponseEntity createFeed (@RequestBody @Valid CreateFeedDTO dto){
        Feed feed = feedService.createFeed(dto);

        return ResponseEntity.created(null).body(feed);
    }

    @GetMapping("/id")
    public ResponseEntity getAFeed (){

    }


}
