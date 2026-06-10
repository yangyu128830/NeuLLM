import http from './http';

export default {
    sendEmail(data) {
        return http.post('/api/sendEmail', data);
    },
    confirmTravelReminder(data) {
        return http.post('/api/travelReminder/confirm', data);
    },
    confirmHotelBook(data) {
        return http.post('/api/hotelBook/confirm', data);
    },
    saveTravelReminder(data) {
        return http.post('/api/travelReminder', data);
    },
    getTravelReminders() {
        return http.get('/api/travelReminders');
    },
    deleteTravelReminder(id) {
        return http.delete(`/api/travelReminder/${id}`);
    },
    saveUserProfile(data) {
        return http.post('/api/userProfile', data);
    },
    lookupUserProfile(name) {
        return http.get('/api/userProfile/lookup', { params: { name } });
    }
};
