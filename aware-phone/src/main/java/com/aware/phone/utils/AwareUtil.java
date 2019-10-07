package com.aware.phone.utils;

import com.aware.utils.Http;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

/**
 * Utility class for AWARE-specific processes.
 */
public class AwareUtil {

    /**
     *
     * @param studyUrl
     * @return
     */
    public static JSONObject getStudyConfig(String studyUrl) throws JSONException {
        if (studyUrl.contains("drive.google.com/file")) {
            // TODO RIO: Take care of Google drive URLs
            String fileId = "1sejHjxRKUnjTN6vYXgSe6R4OIXUHVRMq";
            studyUrl = "https://drive.google.com/uc?id=" + fileId;
        }

        String request = new Http().dataGET(studyUrl, false);
        if (request != null) {
            return new JSONObject(request);
        }
        return null;
    }

    /**
     * Validates that the study config has the correct JSON schema for AWARE.
     * It needs to have the keys: "database", "sensors" and "study_info".
     *
     * @param config JSON representing a study configuration
     * @return true if the study config is valid, false otherwise
     */
    public static boolean validateStudyConfig(JSONObject config) {
        // TODO: Add validation for certificate URLs
        String[] requiredKeys = {"database", "sensors", "study_info"};
        for (String key: requiredKeys) {
            if (!config.has(key)) return false;
        }
        return true;
    }
}
