package com.neusoft.edu.neullmdev.controller.reminder;

import com.neusoft.edu.neullmdev.dto.reminder.TravelReminderRequest;
import com.neusoft.edu.neullmdev.dto.reminder.TravelReminderResponse;
import com.neusoft.edu.neullmdev.entity.reminder.TravelReminderEntity;
import com.neusoft.edu.neullmdev.model.api.ApiResponse;
import com.neusoft.edu.neullmdev.service.reminder.TravelReminderConverter;
import com.neusoft.edu.neullmdev.service.reminder.TravelReminderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TravelReminderController {

    private final TravelReminderService travelReminderService;

    public TravelReminderController(TravelReminderService travelReminderService) {
        this.travelReminderService = travelReminderService;
    }

    @PostMapping("/travelReminder")
    public ResponseEntity<ApiResponse<TravelReminderResponse>> createReminder(@RequestBody TravelReminderRequest request) {
        TravelReminderEntity saved = travelReminderService.save(TravelReminderConverter.toEntity(request));
        return ResponseEntity.ok(ApiResponse.success("提醒创建成功", TravelReminderConverter.toResponse(saved)));
    }

    @GetMapping("/travelReminders")
    public ResponseEntity<ApiResponse<List<TravelReminderResponse>>> getAllReminders() {
        List<TravelReminderResponse> list = travelReminderService.findAll().stream()
                .map(TravelReminderConverter::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/travelReminder/{id}")
    public ResponseEntity<ApiResponse<TravelReminderResponse>> getReminderById(@PathVariable String id) {
        TravelReminderEntity reminder = travelReminderService.findById(id);
        if (reminder == null) {
            return ResponseEntity.status(404).body(ApiResponse.fail("提醒不存在"));
        }
        return ResponseEntity.ok(ApiResponse.success(TravelReminderConverter.toResponse(reminder)));
    }

    @DeleteMapping("/travelReminder/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(@PathVariable String id) {
        if (travelReminderService.findById(id) == null) {
            return ResponseEntity.status(404).body(ApiResponse.fail("提醒不存在"));
        }
        travelReminderService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }

    @PutMapping("/travelReminder/{id}")
    public ResponseEntity<ApiResponse<TravelReminderResponse>> updateReminder(
            @PathVariable String id, @RequestBody TravelReminderRequest request) {
        if (travelReminderService.findById(id) == null) {
            return ResponseEntity.status(404).body(ApiResponse.fail("提醒不存在"));
        }
        TravelReminderEntity entity = TravelReminderConverter.toEntity(request);
        entity.setId(id);
        TravelReminderEntity updated = travelReminderService.update(entity);
        return ResponseEntity.ok(ApiResponse.success("更新成功", TravelReminderConverter.toResponse(updated)));
    }
}
