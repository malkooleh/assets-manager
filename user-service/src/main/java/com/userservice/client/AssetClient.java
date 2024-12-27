package com.userservice.client;

import com.userservice.model.client.response.AssetsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "assets-service")
public interface AssetClient {

    @GetMapping("/assets/users/{userId}")
    AssetsResponse getUserAssets(@PathVariable("userId") Integer userId);
}
