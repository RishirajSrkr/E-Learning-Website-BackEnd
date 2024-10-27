package com.rishiraj.bitbybit.entity;

import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.cglib.core.Local;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = "blacklistedTokens")
public class BlacklistedToken {
    @Id
    private ObjectId id;
    private String token;
    private LocalDateTime expiryDate;

    public BlacklistedToken (String token, LocalDateTime expiryDate){
        this.token = token;
        this.expiryDate = expiryDate;
    }
}
