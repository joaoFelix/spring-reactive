package com.reactivespring.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.reactivespring.domain.Review;

import reactor.core.publisher.Flux;

@Repository
public interface ReviewReactiveRepository extends ReactiveMongoRepository<Review, String> {
    Flux<Review> findAllByMovieInfoId(Long movieInfoId);
}
