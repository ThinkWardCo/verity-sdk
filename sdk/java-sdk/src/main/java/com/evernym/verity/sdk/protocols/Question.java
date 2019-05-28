package com.evernym.verity.sdk.protocols;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.jcajce.provider.digest.SHA3.DigestSHA3;
import org.bouncycastle.util.encoders.Hex;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Builds and sends a new encrypted agent message for the Question protocol.
 */
public class Question extends Protocol {
    // Message Type Definitions
    public static String ASK_QUESTION_MESSAGE_TYPE = "vs.service/question/0.1/question";
    public static String PROBLEM_REPORT_MESSAGE_TYPE = "vs.service/question/0.1/problem-report";
    public static String STATUS_MESSAGE_TYPE = "vs.service/question/0.1/status";

    // Status Definitions
    public static Integer QUESTION_SENT_STATUS_STATE = 0;
    public static Integer QUESTION_ANSWERED_STATE = 1;
    public static Integer ERROR_STATE = 2;

    private String connectionId;
    private String notificationTitle;
    private String questionText;
    private String questionDetail;
    private JSONArray validResponses;

    /**
     * Create a new Question object
     * @param connectionId the pairwise DID of the connection you want to send the question to
     * @param notificationTitle the title of the push notification (currently only rendered in Connect.Me when questionDetail is omitted)
     * @param questionText The main text of the question (included in the message the Connect.Me user signs with their private key)
     * @param questionDetail Any additional information about the question
     * @param validResponses The possible responses. See the Verity Protocol documentation for more information on how Connect.Me will render these options.
     * @throws UnsupportedEncodingException when the encoding of the question_text and valid_responses are not encoded in a a supported format (utf-8 recommended)
     */
    public Question(String connectionId, String notificationTitle, String questionText, String questionDetail,
            String[] validResponses) throws UnsupportedEncodingException {
        super();
        this.connectionId = connectionId;
        this.notificationTitle = notificationTitle;
        this.questionText = questionText;
        this.questionDetail = questionDetail;
        this.validResponses = new JSONArray();
        for (String validResponseString : validResponses) {
            JSONObject validResponse = new JSONObject();
            validResponse.put("text", validResponseString);
            validResponse.put("nonce", getHashedMessage(this.questionText, validResponseString));
            this.validResponses.put(validResponse);
        }
    }

    private String getHashedMessage(String questionText, String responseOption) throws UnsupportedEncodingException {
        DigestSHA3 sha3256 = new SHA3.Digest256();
        sha3256.update(questionText.getBytes("UTF-8"));
        sha3256.update(responseOption.getBytes("UTF-8"));
        sha3256.update(UUID.randomUUID().toString().getBytes("UTF-8"));
        return Hex.toHexString(sha3256.digest());
    }

    /**
     * Returns the JSON string of the Question message
     */
    @Override
    public String toString() {
        JSONObject message = new JSONObject();
        message.put("@type", Question.ASK_QUESTION_MESSAGE_TYPE);
        message.put("@id", this.id);
        message.put("connection_id", this.connectionId);
        JSONObject question = new JSONObject();
        question.put("notification_title", this.notificationTitle);
        question.put("question_text", this.questionText);
        question.put("question_detail", this.questionDetail);
        question.put("valid_responses", this.validResponses);
        message.put("question", question);
        return message.toString();
    }
}