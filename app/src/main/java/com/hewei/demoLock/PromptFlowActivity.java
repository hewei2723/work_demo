package com.hewei.demoLock;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class PromptFlowActivity extends AppCompatActivity {

    private TextView tvJsonContent;
    private ImageButton btnBack;
    private ScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_prompt_flow);

        initViews();
        loadJsonContent();
    }

    private void initViews() {
        tvJsonContent = findViewById(R.id.tvJsonContent);
        btnBack = findViewById(R.id.btnBack);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadJsonContent() {
        tvJsonContent.setText("正在加载...");

        new Thread(() -> {
            try {
                // 从 assets 读取 JSON 文件
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(getAssets().open("prompt_flow.json")));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                }
                reader.close();

                String jsonString = stringBuilder.toString();

                // 解析并提取对话内容
                String formattedContent = extractConversation(jsonString);

                // 在 UI 线程更新显示
                runOnUiThread(() -> {
                    tvJsonContent.setText(formattedContent);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "加载失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    tvJsonContent.setText("加载失败: " + e.getMessage());
                });
            }
        }).start();
    }

    /**
     * 从 JSON 中提取对话内容
     */
    private String extractConversation(String jsonString) {
        StringBuilder result = new StringBuilder();

        try {
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray messages = jsonObject.optJSONArray("messages");

            if (messages == null) {
                return "未找到对话记录";
            }

            for (int i = 0; i < messages.length(); i++) {
                JSONObject msg = messages.getJSONObject(i);
                String type = msg.optString("type", "");

                // 只处理用户和 AI 的消息
                if ("user".equals(type)) {
                    result.append("════════════════════════════════════════\n");
                    result.append("👤 用户:\n\n");
                    result.append(extractUserContent(msg)).append("\n");

                } else if ("assistant".equals(type)) {
                    result.append("════════════════════════════════════════\n");
                    result.append("🤖 AI:\n\n");
                    result.append(extractAssistantContent(msg)).append("\n");
                }
            }

        } catch (Exception e) {
            return "解析失败: " + e.getMessage();
        }

        return result.toString();
    }

    /**
     * 提取用户消息内容
     */
    private String extractUserContent(JSONObject msg) {
        try {
            JSONObject message = msg.getJSONObject("message");
            JSONArray contentArray = message.getJSONArray("content");

            StringBuilder content = new StringBuilder();
            for (int i = 0; i < contentArray.length(); i++) {
                JSONObject item = contentArray.getJSONObject(i);
                String itemType = item.optString("type", "");

                if ("text".equals(itemType)) {
                    String text = item.optString("text", "");
                    content.append(text);
                }
            }

            return content.toString();

        } catch (Exception e) {
            return "[提取用户消息失败]";
        }
    }

    /**
     * 提取 AI 回复内容
     */
    private String extractAssistantContent(JSONObject msg) {
        try {
            JSONObject message = msg.getJSONObject("message");
            JSONArray contentArray = message.getJSONArray("content");

            StringBuilder content = new StringBuilder();

            for (int i = 0; i < contentArray.length(); i++) {
                JSONObject item = contentArray.getJSONObject(i);
                String itemType = item.optString("type", "");

                if ("thinking".equals(itemType)) {
                    // 思考过程
                    String thinking = item.optString("thinking", "");
                    content.append("🧠 思考过程:\n").append(thinking).append("\n\n");

                } else if ("text".equals(itemType)) {
                    // 文本回复
                    String text = item.optString("text", "");
                    content.append(text);
                }
            }

            return content.toString();

        } catch (Exception e) {
            return "[提取 AI 回复失败]";
        }
    }
}
