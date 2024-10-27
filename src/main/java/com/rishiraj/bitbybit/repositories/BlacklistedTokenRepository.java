package com.rishiraj.bitbybit.repositories;

import com.rishiraj.bitbybit.entity.BlacklistedToken;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface BlacklistedTokenRepository extends MongoRepository<BlacklistedToken, ObjectId> {
    Optional<BlacklistedToken> findByToken(String token);
}
