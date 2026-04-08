package com.example.userservice.config.provider;

import in.zeta.oms.sandbox.model.object.ObjectProvider;
import in.zeta.oms.sandbox.model.realm.Realm;
import olympus.common.JID;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
public class UserProvider implements ObjectProvider<SandboxUser> {

    public static final String OBJECT_TYPE = "SplitwiseAmitCipherUser";

    @Override
    public CompletionStage<Optional<SandboxUser>> getObject(JID jid, Realm realm, Long tenantID) {
        long userId = 1L;
        SandboxUser dummyUser = SandboxUser.builder()
                .userId(1L)
                .name("User-" + userId)
                .email("user" + userId + "@example.com")
                .phone("0000000000")
                .globalCreditLimit(0.0)
                .status("ACTIVE")
                .build();
        
        return CompletableFuture.completedFuture(Optional.of(dummyUser));
    }
}
