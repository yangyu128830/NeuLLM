package com.neusoft.edu.neullmdev.controller.notification;

import com.neusoft.edu.neullmdev.model.api.ApiResponse;
import com.neusoft.edu.neullmdev.service.notification.NotificationService;
import com.neusoft.edu.neullmdev.service.notification.NotificationSummaryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final NotificationSummaryService notificationSummaryService;

    public NotificationController(NotificationService notificationService,
                                  NotificationSummaryService notificationSummaryService) {
        this.notificationService = notificationService;
        this.notificationSummaryService = notificationSummaryService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list(
            @RequestParam(defaultValue = "50") int limit) {
        return ApiResponse.success(notificationService.listForCurrentUser(limit));
    }

    @GetMapping("/summary")
    public ApiResponse<Map<String, Object>> summary(
            @RequestParam(defaultValue = "20") int limit) {
        return ApiResponse.success(notificationSummaryService.summarizeForCurrentUser(limit));
    }

    @GetMapping("/unread-count")
    public ApiResponse<Map<String, Object>> unreadCount() {
        return ApiResponse.success(Map.of("count", notificationService.unreadCountForCurrentUser()));
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Map<String, Object>> markRead(@PathVariable Long id) {
        boolean ok = notificationService.markRead(id);
        return ApiResponse.success(Map.of("ok", ok));
    }

    @PostMapping("/read-all")
    public ApiResponse<Map<String, Object>> markAllRead() {
        int n = notificationService.markAllRead();
        return ApiResponse.success(Map.of("updated", n));
    }
}
