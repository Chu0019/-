# 桃園國際機場空側駕駛許可證測驗 App
(Taoyuan International Airport Airside Driving License Quiz App)

這是一個專為準備考取「桃園國際機場空側駕駛許可證」人員設計的測驗應用程式。本專案同時包含了 **iOS (SwiftUI)** 與 **Android (Jetpack Compose)** 雙平台的原生應用程式實作。

App 內建完整的 304 題最新題庫，幫助駕駛員隨時隨地在手機上進行測驗與複習，無需註冊登入即可直接使用。

## 📱 核心功能 (Shared Features)

雙平台皆實作了以下核心測驗功能：

* **完整題庫支援**：內建所有交通道規則、安全規範與標誌手勢的 304 題精選考題與圖文。
* **隨機抽題測驗**：每次測驗皆自動從題庫中隨機抽出 20 題，模擬真實考試體驗。
* **選項隨機打亂**：為了達到最佳學習效果，每一題的四個選項順序會自動洗牌，避免使用者死記硬背位置。
* **圖文並茂**：支援針對特殊題型的附圖顯示，幫助理解標誌與現場規範。
* **即時成績結算**：答題完畢後立即計算得分。
* **錯題整理機制**：測驗成績畫面中，會優先把「答錯的題目」置頂顯示，並以特別色標示您的錯誤答案與正確答案，幫助加強記憶。
* **一鍵重新測驗**：成績頁面提供「重新測試」功能，無須退回首頁即可立刻開始下一輪全新挑戰。
* **排版最佳化**：針對中文考題文字進行了防裁切與換行處理。

---

## 🍎 iOS 版本 (iOS Version)

iOS 版本採用現代化的宣告式 UI 框架 SwiftUI 打造。

### 🛠 開發技術 (Tech Stack - iOS)
* **語言**: Swift 5+
* **UI 框架**: SwiftUI
* **架構**: MVVM (Model-View-ViewModel)
* **最低版本需求**: iOS 16.0+

### 📂 專案結構 (Project Structure - iOS)
* `機場駕照.xcodeproj`: Xcode 專案檔。
* ` AirportQuizApp.swift`: App 切入點 (Entry point)
* ` ContentView.swift`: 包含首頁 (`StartView`)、測驗單元 (`QuizView`) 以及結算與複習畫面 (`ResultsView`) 的所有 UI 元件與邏輯。
* ` Questions.swift`: 輕量化的本地端題庫，定義了 `Question` 資料模型並包含所有題目陣列。
* ` Assets.xcassets`: 存放所有的圖示與題庫附帶之影像檔案資源 (`qX_imageXX.jpg`/`.png`)。

### 🚀 執行與測試 (How to Run - iOS)
1. 確認您已安裝 **Xcode 15** 或更新版本，並使用 macOS 系統。
2. 點擊 `機場駕照.xcodeproj` 開啟專案。
3. 在 Xcode 上方裝置選單中選擇您的 iPhone 或任意 iOS 模擬器。
4. 按下 `Cmd + R` 或點擊左上方的「Play」按鈕編譯並執行 App。

---

## 🤖 Android 版本 (Android Version)

Android 版本採用 Google 推出的現代化 UI 工具包 Jetpack Compose 重新刻劃相同的設計。

### 🛠 開發技術 (Tech Stack - Android)
* **語言**: Kotlin
* **UI 框架**: Jetpack Compose
* **架構**: MVVM (Model-View-ViewModel) + Android Architecture Components (ViewModel)
* **依賴管理**: Gradle (Kotlin DSL)
* **最低版本需求**: Android API 24 (Android 7.0)

### 📂 專案結構 (Project Structure - Android)
* `AndroidApp/`: Android 專案的根目錄。
* ` app/src/main/java/.../MainActivity.kt`: 包含所有的 Compose UI 元件，包括起點畫面 (`StartView`)、測驗畫面 (`QuizView`) 和結果畫面 (`ResultsView`)。
* ` app/src/main/java/.../QuizViewModel.kt`: 負責測驗狀態管理、計分、隨機抽取、選項打亂與錯題排序邏輯。
* ` app/src/main/java/.../Questions.kt`: 題庫資料結構與完整的題目及影像對應。
* ` app/src/main/res/drawable/`: 存放對應題庫的所有影像圖檔資源。

### 🚀 執行與測試 (How to Run - Android)
1. 下載並安裝最新版的 **Android Studio** (建議 Hedgehog 或更新版本)。
2. 在 Android Studio 起始畫面選擇 **Open**，然後瀏覽並選擇本專案目錄下的 `AndroidApp` 資料夾。
3. 等待 Gradle 同步完成 (Sync Project with Gradle Files)。
4. 連接您的 Android 實體裝置，或透過 Device Manager 啟動一台 Android 模擬器 (AVD)。
5. 按下 `Shift + F10` 或點選上方工具列的「Run 'app'」綠色箭頭按鈕編譯並執行 App。

---

## 📝 備註事項

本測驗 App 主要供學習交流及考試練習使用，所有考題僅供模擬參考。實際交通規範請以桃園國際機場公司官方發布之「空側駕駛資格許可作業程序」為準。
