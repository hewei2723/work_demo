# 锁屏应用 (Lock Screen App)

一个自定义 Android 锁屏应用，支持时间显示、通知展示和滑动解锁功能。

## 功能特性

- 📱 **自定义锁屏界面** - 美观的渐变背景和时间日期显示
- 🔔 **通知中心** - 动态显示应用通知（模拟微信、QQ等）
- 🔓 **滑动解锁** - 流畅的向上滑动解锁体验
- ⚡ **后台持久运行** - 前台服务，应用被清理后仍可运行
- 🚀 **开机自启动** - 设备重启后自动启动锁屏服务
- 🔐 **二次解锁** - 系统锁屏 → 自定义锁屏 → 滑动解锁

## 运行方式

### 环境要求

- Android Studio 2022.3 或更高版本
- Android SDK API 24 (Android 7.0) 或更高
- Gradle 8.0+

### 运行步骤

1. **克隆项目**
   ```bash
   git clone https://github.com/hewei2723/work_demo.git
   cd lock
   ```

2. **打开项目**
   - 使用 Android Studio 打开项目
   - 等待 Gradle 同步完成

3. **运行应用**
   - 连接 Android 设备或启动模拟器
   - 点击 Run 按钮（或按 Shift + F10）
   - 应用安装完成后自动启动

4. **使用应用**
   - 点击"启动锁屏服务"
   - 按电源键关闭屏幕
   - 再按电源键点亮屏幕
   - 输入系统锁屏密码（如果有）
   - 看到自定义锁屏界面
   - 向上滑动底部圆圈解锁

## Google 原生 Android 后台持久运行指南

### 核心权限说明

为了让应用在 Google 原生 Android 系统上长期稳定运行，需要引导用户开启以下权限：

---

### 1. 悬浮窗权限 (SYSTEM_ALERT_WINDOW) ⭐ 必须

**作用**：允许应用在其他应用和锁屏界面上显示内容

**开启方式**：
- 应用内点击"权限设置" → "悬浮窗权限" → "去设置"
- 或手动：设置 → 应用 → 特殊访问 → 悬浮在其他应用上层 → 允许

**重要性**：🔴 **必须** - 没有此权限无法在锁屏上显示

---

### 2. 忽略电池优化 ⭐ 必须

**作用**：防止系统因省电策略杀死后台服务

**开启方式**：
- 应用内点击"权限设置" → "忽略电池优化" → "去设置" → 选择"不优化"
- 或手动：设置 → 电池 → 电池优化 → 锁屏应用 → "不优化"

**重要性**：🔴 **必须** - Google 原生 Android 会严格限制后台应用

---

### 3. 通知权限 (POST_NOTIFICATIONS) ⭐ 必须 (Android 13+)

**作用**：允许前台服务显示通知，保持服务运行

**开启方式**：
- 首次启动应用时会自动弹出权限请求
- 或手动：设置 → 应用 → 锁屏应用 → 通知 → 允许

**重要性**：🔴 **必须** - Android 13+ 必须授予才能显示通知

---

### 4. 后台运行权限 (允许后台活动)

**作用**：允许应用在后台持续运行

**开启方式**：
- 设置 → 应用 → 锁屏应用 → 电池 → "无限制" 或 "允许后台活动"
- 部分设备：设置 → 应用 → 锁屏应用 → 启动管理 → 允许后台活动

**重要性**：🟡 **推荐** - 可显著提高服务存活率

---

### 5. 自启动权限 ⭐ 推荐

**作用**：设备重启后自动启动服务

**开启方式**：
- Google 原生 Android 默认已允许
- 部分 ROM：设置 → 应用 → 锁屏应用 → 自动启动

**重要性**：🟡 **推荐** - 方便用户无需手动启动

---

### 权限检查清单

用户首次使用时，应用会引导检查以下权限：

| 权限 | 必须性 | 检测方式 |
|------|--------|----------|
| 悬浮窗权限 | 🔴 必须 | 自动检测 |
| 忽略电池优化 | 🔴 必须 | 自动检测 |
| 通知权限 | 🔴 必须 | 自动检测 |
| 后台运行 | 🟡 推荐 | 需手动设置 |
| 自启动 | 🟡 推荐 | 需手动设置 |

---

## 用户引导流程

### 首次启动流程

1. **启动应用** → 点击"启动锁屏服务"

2. **自动权限检查**：
   - 如果悬浮窗权限未授予，自动跳转到设置页面
   - 用户授予后返回应用

3. **服务启动** → 状态栏显示"锁屏服务运行中"通知

4. **推荐操作**（可选）：
   - 点击"权限设置"
   - 开启"忽略电池优化" → 选择"不优化"
   - 确保"允许后台活动"已开启

5. **测试功能**：
   - 按电源键锁屏
   - 再按电源键解锁
   - 看到自定义锁屏界面

---

## 技术实现

### 前台服务

应用使用前台服务确保后台持久运行：

```java
// 创建通知渠道（Android 8.0+）
NotificationChannel channel = new NotificationChannel(
    "lockscreen_service_channel",
    "锁屏服务",
    NotificationManager.IMPORTANCE_LOW
);

// 启动前台服务
startForeground(NOTIFICATION_ID, notification);
```

### 屏幕监听

通过 BroadcastReceiver 监听屏幕状态：

```java
// 监听屏幕点亮
Intent.ACTION_SCREEN_ON

// 监听屏幕关闭
Intent.ACTION_SCREEN_OFF

// 监听用户解锁
Intent.ACTION_USER_PRESENT
```

### 锁屏显示

使用 `FLAG_SHOW_WHEN_LOCKED` 在锁屏上显示：

```java
// Android 10+
setShowWhenLocked(true);
setTurnScreenOn(true);

// 旧版本
getWindow().addFlags(FLAG_SHOW_WHEN_LOCKED);
```

---

## 项目结构

```
app/src/main/java/com/hewei/demoLock/
├── MainActivity.java              # 主控制界面
├── LockScreenActivity.java        # 锁屏界面
├── LockScreenService.java         # 前台服务
├── ScreenReceiver.java            # 屏幕状态监听
├── BootReceiver.java              # 开机启动监听
├── PermissionsActivity.java       # 权限管理界面
├── PromptFlowActivity.java        # 提示词流程展示
└── UnlockSliderView.java          # 滑动解锁组件

app/src/main/res/layout/
├── activity_main_control.xml      # 主控制界面
├── activity_main.xml              # 锁屏界面布局
├── activity_permissions.xml       # 权限设置界面
├── activity_prompt_flow.xml       # 提示词展示界面
├── notification_item.xml          # 通知卡片布局
└── unlock_slider_view.xml         # 滑动解锁布局
```

---

## 常见问题

### Q: 为什么锁屏界面不显示？

**A**: 可能原因：
1. 未授予悬浮窗权限
2. 系统锁屏有密码，需要先输入密码
3. 服务未启动，检查"权限设置"中的服务状态

### Q: 为什么后台服务被杀死？

**A**: Google 原生 Android 严格限制后台应用，请确保：
1. 开启"忽略电池优化"
2. 应用在"允许后台活动"列表中
3. 不要使用"强力清理"功能

### Q: 通知栏有"锁屏服务运行中"可以关闭吗？

**A**: 不建议关闭。这是前台服务的必要条件，关闭后服务可能会被系统杀死。如要隐藏，可在通知设置中将优先级设为"低"。

### Q: 不同品牌手机如何设置？

**A**: 虽然本指南针对 Google 原生 Android，但各品牌手机路径略有不同：
- **小米**：安全中心 → 自启动管理
- **华为**：手机管家 → 启动管理
- **OPPO**：安全中心 → 自启动管理
- **vivo**：i管家 → 自启动

---

## 开源许可

本项目仅供学习和参考使用。

---

## 版本信息

- **版本**：1.0.0
- **最低 SDK**：API 24 (Android 7.0)
- **目标 SDK**：API 34 (Android 14)
