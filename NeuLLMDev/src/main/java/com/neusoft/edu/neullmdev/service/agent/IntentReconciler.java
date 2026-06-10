package com.neusoft.edu.neullmdev.service.agent;

import com.neusoft.edu.neullmdev.auth.AuthUser;
import com.neusoft.edu.neullmdev.model.agent.FunctionCall;
import com.neusoft.edu.neullmdev.service.agent.intent.ClassroomIntentOverride;
import com.neusoft.edu.neullmdev.service.agent.intent.HotelFoodIntentOverride;
import com.neusoft.edu.neullmdev.service.agent.intent.PlanTripIntentOverride;
import com.neusoft.edu.neullmdev.service.agent.intent.SendEmailIntentOverride;
import com.neusoft.edu.neullmdev.service.agent.intent.TravelPrepWeatherOverride;
import com.neusoft.edu.neullmdev.service.agent.intent.WeatherIntentOverride;
import org.springframework.stereotype.Component;

/**
 * 统一入口：按固定顺序纠偏 LLM 解析出的函数调用（不执行工具）。
 */
@Component
public class IntentReconciler {

    private final SendEmailIntentOverride sendEmail;
    private final HotelFoodIntentOverride hotelFood;
    private final WeatherIntentOverride weather;
    private final PlanTripIntentOverride planTrip;
    private final TravelPrepWeatherOverride travelPrepWeather;
    private final ClassroomIntentOverride classroom;

    public IntentReconciler(SendEmailIntentOverride sendEmail,
                            HotelFoodIntentOverride hotelFood,
                            WeatherIntentOverride weather,
                            PlanTripIntentOverride planTrip,
                            TravelPrepWeatherOverride travelPrepWeather,
                            ClassroomIntentOverride classroom) {
        this.sendEmail = sendEmail;
        this.hotelFood = hotelFood;
        this.weather = weather;
        this.planTrip = planTrip;
        this.travelPrepWeather = travelPrepWeather;
        this.classroom = classroom;
    }

    public FunctionCall reconcile(String userInput, FunctionCall parsed) {
        return reconcile(userInput, parsed, false);
    }

    public FunctionCall reconcile(String userInput, FunctionCall parsed, boolean teacherMode) {
        return reconcile(userInput, parsed, teacherMode, null);
    }

    public FunctionCall reconcile(String userInput, FunctionCall parsed, boolean teacherMode, AuthUser authUser) {
        FunctionCall current = classroom.reconcile(userInput, parsed, teacherMode, authUser);
        if (teacherMode) {
            return current;
        }
        current = sendEmail.reconcile(userInput, current);
        current = hotelFood.reconcile(userInput, current);
        current = weather.reconcile(userInput, current);
        current = planTrip.reconcile(userInput, current);
        return travelPrepWeather.reconcile(userInput, current);
    }
}
