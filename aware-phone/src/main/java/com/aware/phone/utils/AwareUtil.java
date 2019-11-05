package com.aware.phone.utils;

import android.content.Context;

import com.aware.utils.Http;
import com.aware.utils.Jdbc;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for AWARE-specific processes.
 */
public class AwareUtil {
    private static final String[] REQUIRED_STUDY_CONFIG_KEYS = {"database", "sensors", "study_info"};

    /**
     *
     * @param studyUrl
     * @return
     */
    public static JSONObject getStudyConfig(String studyUrl) throws JSONException {
        if (studyUrl.contains("drive.google.com/file")) {
//            Pattern pattern = Pattern.compile("(?<=\\/d\\/).*(?=\\/)");
//            Matcher matcher = pattern.matcher(studyUrl);
//            System.out.println("Match found: " + matcher.find());
//            studyUrl = matcher.group(1);
//            System.out.println("Input String matches regex - " + studyUrl);
            String fileId = studyUrl.replace("/view?usp=sharing", "");
            fileId = fileId.replace("https://drive.google.com/file/d/", "");

            // TODO: Take care of Google drive URLs
//            String fileId = "1sejHjxRKUnjTN6vYXgSe6R4OIXUHVRMq";
            studyUrl = "https://drive.google.com/uc?export=download&id=" + fileId;
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
     * @param context application context
     * @param config JSON representing a study configuration
     * @return true if the study config is valid, false otherwise
     */
    public static boolean validateStudyConfig(Context context, JSONObject config) {
        // TODO: Add validation for certificate URLs
        for (String key: REQUIRED_STUDY_CONFIG_KEYS) {
            if (!config.has(key)) return false;
        }

        // Test database connection
        try {
            JSONObject dbInfo = config.getJSONObject("database");
            return Jdbc.testConnection(context, dbInfo.getString("database_host"),
                    dbInfo.getString("database_port"), dbInfo.getString("database_name"),
                    dbInfo.getString("database_username"), dbInfo.getString("database_password"));
        } catch (JSONException e) {
            return false;
        }
    }

    /**
     * Returns the mapping of a sensor setting (from study config) to its sensor's name.
     * (e.g. status_accelerometer -> accelerometer)
     *
     * @param setting
     * @return
     */
    public static String getSensorType(String setting) {
        // TODO: Get a proper mapping
        return setting.replace("status_", "");
    }
}
