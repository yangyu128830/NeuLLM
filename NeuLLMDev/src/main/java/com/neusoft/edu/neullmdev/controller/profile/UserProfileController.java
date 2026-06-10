package com.neusoft.edu.neullmdev.controller.profile;

import com.fasterxml.jackson.databind.JsonNode;
import com.neusoft.edu.neullmdev.dto.profile.UserProfileLookupResult;
import com.neusoft.edu.neullmdev.dto.profile.UserProfileSaveParams;
import com.neusoft.edu.neullmdev.service.mcp.McpCommitService;
import com.neusoft.edu.neullmdev.service.profile.UserProfileService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class UserProfileController {

    private final McpCommitService mcpCommitService;
    private final UserProfileService userProfileService;

    public UserProfileController(McpCommitService mcpCommitService, UserProfileService userProfileService) {
        this.mcpCommitService = mcpCommitService;
        this.userProfileService = userProfileService;
    }

    @PostMapping(value = "/userProfile", produces = MediaType.APPLICATION_JSON_VALUE)
    public JsonNode saveUserProfile(@RequestBody UserProfileSaveParams params) throws Exception {
        return mcpCommitService.commitTool("save_user_profile", params);
    }

    @GetMapping(value = "/userProfile/lookup", produces = MediaType.APPLICATION_JSON_VALUE)
    public UserProfileLookupResult lookupUserProfile(@RequestParam(value = "name", required = false) String name) {
        return userProfileService.lookupByName(name);
    }
}
