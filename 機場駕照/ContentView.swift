import SwiftUI
import Combine

struct QuizQuestion: Identifiable {
    let id: Int
    let q: String
    let shuffledOptions: [String]
    let correctIndex: Int
    let image: String?
}

class QuizViewModel: ObservableObject {
    @Published var questions: [QuizQuestion] = []
    @Published var currentIndex = 0
    @Published var selectedAnswers: [Int?] = []
    @Published var showResults = false
    @Published var score = 0
    
    func startQuiz() {
        // 從題庫隨機抽取 20 題
        let rawQuestions = questionBank.shuffled().prefix(min(20, questionBank.count))
        
        self.questions = rawQuestions.map { q in
            // 隨機打亂選項
            let originalOptions = q.options.filter { !$0.isEmpty }
            let indices = Array(0..<originalOptions.count).shuffled()
            let shuffledOptions = indices.map { originalOptions[$0] }
            
            // 找出正確答案在打亂後的新位置
            let correctIndex = indices.firstIndex(of: q.answer) ?? 0
            
            return QuizQuestion(
                id: q.id,
                q: q.q,
                shuffledOptions: shuffledOptions,
                correctIndex: correctIndex,
                image: q.image
            )
        }
        
        self.selectedAnswers = Array(repeating: nil, count: self.questions.count)
        self.currentIndex = 0
        self.showResults = false
        self.score = 0
    }
    
    var sortedResultsIndices: [Int] {
        questions.indices.sorted { i, j in
            let isCorrectI = selectedAnswers[i] == questions[i].correctIndex
            let isCorrectJ = selectedAnswers[j] == questions[j].correctIndex
            if !isCorrectI && isCorrectJ { return true }
            if isCorrectI && !isCorrectJ { return false }
            return i < j
        }
    }
    
    func submit() {
        var calculatedScore = 0
        for (index, q) in questions.enumerated() {
            if selectedAnswers[index] == q.correctIndex {
                calculatedScore += 1
            }
        }
        self.score = calculatedScore
        self.showResults = true
    }
}

struct PasswordView: View {
    @Binding var isAuthenticated: Bool
    @State private var password = ""
    @State private var errorMessage: String? = nil
    @State private var isLoading = false
    
    var body: some View {
        VStack(spacing: 24) {
            Image(systemName: "lock.shield.fill")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 80, height: 80)
                .foregroundStyle(.indigo)
                .shadow(radius: 5)
            
            Text("安全驗證")
                .font(.title2)
                .fontWeight(.bold)
                .foregroundStyle(Color(hex: "0f172a"))
            
            SecureField("請輸入進入密碼", text: $password)
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .padding(.horizontal)
                .disabled(isLoading)
            
            if let error = errorMessage {
                Text(error)
                    .font(.footnote)
                    .foregroundStyle(.red)
            }
            
            Button(action: verifyPassword) {
                if isLoading {
                    ProgressView()
                        .progressViewStyle(CircularProgressViewStyle(tint: .white))
                        .frame(maxWidth: .infinity)
                } else {
                    Text("登入")
                        .font(.headline)
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                }
            }
            .frame(height: 50)
            .background(password.isEmpty ? Color.gray : Color.indigo)
            .cornerRadius(12)
            .padding(.horizontal)
            .disabled(password.isEmpty || isLoading)
        }
        .padding(30)
        .background(Color.white.opacity(0.9))
        .cornerRadius(20)
        .shadow(radius: 10)
        .padding()
    }
    
    private func verifyPassword() {
        guard !password.isEmpty else { return }
        isLoading = true
        errorMessage = nil
        
        guard let url = URL(string: "https://raw.githubusercontent.com/Chu0019/-/main/password.txt") else {
            errorMessage = "無效的網址配置"
            isLoading = false
            return
        }
        
        URLSession.shared.dataTask(with: url) { data, response, error in
            DispatchQueue.main.async {
                isLoading = false
                if let _ = error {
                    errorMessage = "無法連線驗證密碼，請檢查網路連線狀態"
                    return
                }
                
                guard let data = data, let fetchedPassword = String(data: data, encoding: .utf8)?.trimmingCharacters(in: .whitespacesAndNewlines) else {
                    errorMessage = "無法讀取密碼資料"
                    return
                }
                
                if password == fetchedPassword {
                    withAnimation {
                        isAuthenticated = true
                    }
                } else {
                    errorMessage = "密碼錯誤，請重新輸入"
                }
            }
        }.resume()
    }
}

struct ContentView: View {
    @StateObject private var viewModel = QuizViewModel()
    @State private var quizStarted = false
    @State private var isAuthenticated = false
    
    var body: some View {
        ZStack {
            // 背景漸層
            LinearGradient(gradient: Gradient(colors: [Color(hex: "e0e7ff"), Color(hex: "f8fafc")]), 
                           startPoint: .topLeading, 
                           endPoint: .bottomTrailing)
                .ignoresSafeArea()
            
            if !isAuthenticated {
                PasswordView(isAuthenticated: $isAuthenticated)
                    .transition(.opacity)
            } else if !quizStarted {
                StartView {
                    viewModel.startQuiz()
                    withAnimation {
                        quizStarted = true
                    }
                }
                .transition(.opacity)
            } else if viewModel.showResults {
                ResultsView(viewModel: viewModel) {
                    withAnimation {
                        quizStarted = false
                    }
                }
                .transition(.asymmetric(insertion: .move(edge: .trailing), removal: .opacity))
            } else {
                QuizView(viewModel: viewModel)
                    .transition(.opacity)
            }
        }
        .preferredColorScheme(.light)
    }
}

struct StartView: View {
    var onStart: () -> Void
    
    var body: some View {
        VStack(spacing: 30) {
            Image(systemName: "airplane.circle.fill")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 100, height: 100)
                .foregroundStyle(.indigo)
                .shadow(radius: 10)
            
            VStack(spacing: 12) {
                Text("桃園國際機場")
                    .font(.title2)
                    .fontWeight(.bold)
                    .foregroundStyle(Color(hex: "64748b"))
                Text("空側駕駛許可證測驗")
                    .font(.system(size: 34, weight: .black))
                    .foregroundStyle(Color(hex: "1e293b")) // 明確指定深色文字
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
            }
            .padding(.horizontal)
            
            VStack(alignment: .leading, spacing: 15) {
                InfoLabel(icon: "list.bullet.clipboard", text: "題庫：共 \(questionBank.count) 題")
                InfoLabel(icon: "timer", text: "每次隨機抽取 20 題")
                InfoLabel(icon: "checkmark.seal", text: "模擬真實測驗環境 (80分及及格)")
            }
            .padding(25)
            .background(Color.white.opacity(0.6))
            .cornerRadius(20)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color.white.opacity(0.5), lineWidth: 1)
            )
            
            Spacer().frame(height: 20)
            
            Button(action: onStart) {
                Text("開始測驗")
                    .font(.title3)
                    .fontWeight(.bold)
                    .foregroundStyle(.white)
                    .frame(maxWidth: .infinity)
                    .padding(.vertical, 18)
                    .background(
                        LinearGradient(colors: [.indigo, .blue], startPoint: .leading, endPoint: .trailing)
                    )
                    .cornerRadius(18)
                    .shadow(color: .indigo.opacity(0.4), radius: 15, x: 0, y: 8)
            }
            .padding(.horizontal, 40)
        }
        .padding()
    }
}

struct QuizView: View {
    @ObservedObject var viewModel: QuizViewModel
    
    var body: some View {
        VStack(spacing: 0) {
            // 頂部進度
            VStack(spacing: 12) {
                HStack {
                    Text("題目 \(viewModel.currentIndex + 1) / \(viewModel.questions.count)")
                        .font(.headline)
                        .foregroundStyle(.indigo)
                    Spacer()
                    Text("\(Int((Double(viewModel.currentIndex + 1) / Double(viewModel.questions.count)) * 100))%")
                        .font(.caption)
                        .fontWeight(.bold)
                        .foregroundStyle(.secondary)
                }
                
                ProgressView(value: Double(viewModel.currentIndex + 1), total: Double(viewModel.questions.count))
                    .tint(.indigo)
            }
            .padding()
            .background(Color.white.opacity(0.4))
            
            ScrollView {
                VStack(alignment: .leading, spacing: 25) {
                    let currentQ = viewModel.questions[viewModel.currentIndex]
                    
                    Text(currentQ.q)
                        .font(.system(size: 21, weight: .bold))
                        .foregroundStyle(Color(hex: "0f172a"))
                        .lineSpacing(6)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    
                    if let imageName = currentQ.image {
                        Image(imageName)
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                            .frame(maxWidth: .infinity)
                            .cornerRadius(15)
                            .shadow(color: Color.black.opacity(0.1), radius: 10, x: 0, y: 5)
                            .padding(.vertical, 5)
                    }
                    
                    VStack(spacing: 14) {
                        ForEach(0..<currentQ.shuffledOptions.count, id: \.self) { index in
                            let option = currentQ.shuffledOptions[index]
                            if !option.isEmpty {
                                OptionButton(
                                    text: option,
                                    isSelected: viewModel.selectedAnswers[viewModel.currentIndex] == index,
                                    action: {
                                        withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                                            viewModel.selectedAnswers[viewModel.currentIndex] = index
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                .padding(20)
            }
            
            // 底部導航
            HStack(spacing: 15) {
                if viewModel.currentIndex > 0 {
                    Button(action: {
                        withAnimation { viewModel.currentIndex -= 1 }
                    }) {
                        Image(systemName: "arrow.left")
                            .font(.title3.bold())
                            .foregroundStyle(.indigo)
                            .frame(width: 56, height: 56)
                            .background(Color.white)
                            .cornerRadius(16)
                            .shadow(color: .black.opacity(0.05), radius: 5)
                    }
                }
                
                Button(action: {
                    if viewModel.currentIndex < viewModel.questions.count - 1 {
                        withAnimation { viewModel.currentIndex += 1 }
                    } else {
                        viewModel.submit()
                    }
                }) {
                    Text(viewModel.currentIndex < viewModel.questions.count - 1 ? "下一題" : "交卷計分")
                        .font(.headline)
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(
                            viewModel.currentIndex < viewModel.questions.count - 1 ?
                            (viewModel.selectedAnswers[viewModel.currentIndex] != nil ? Color.indigo : Color.gray.opacity(0.5)) :
                            Color.green
                        )
                        .cornerRadius(16)
                        .shadow(color: (viewModel.currentIndex < viewModel.questions.count - 1 ? Color.indigo : Color.green).opacity(0.3), radius: 10, x: 0, y: 5)
                }
                .disabled(viewModel.currentIndex < viewModel.questions.count - 1 && viewModel.selectedAnswers[viewModel.currentIndex] == nil)
            }
            .padding()
            .background(Color.white.opacity(0.4))
        }
    }
}

struct ResultsView: View {
    @ObservedObject var viewModel: QuizViewModel
    var onReset: () -> Void
    
    var body: some View {
        VStack(spacing: 0) {
            ScrollView {
                VStack(spacing: 30) {
                    VStack(spacing: 15) {
                        Text("測驗成績")
                            .font(.title2)
                            .fontWeight(.bold)
                            .foregroundStyle(Color(hex: "0f172a"))
                        
                        ZStack {
                            Circle()
                                .stroke(Color.gray.opacity(0.1), lineWidth: 15)
                                .frame(width: 140, height: 140)
                            
                            Circle()
                                .trim(from: 0, to: CGFloat(viewModel.score) / 20.0)
                                .stroke(viewModel.score >= 16 ? Color.green : Color.orange, style: StrokeStyle(lineWidth: 15, lineCap: .round))
                                .frame(width: 140, height: 140)
                                .rotationEffect(.degrees(-90))
                            
                            VStack(spacing: -5) {
                                Text("\(viewModel.score * 5)")
                                    .font(.system(size: 48, weight: .black))
                                    .foregroundStyle(Color(hex: "0f172a"))
                                Text("分")
                                    .font(.headline)
                                    .foregroundStyle(Color(hex: "64748b"))
                            }
                        }
                        
                        Text(viewModel.score >= 16 ? "恭喜通過標核！" : "不及格，請再加油。")
                            .font(.headline)
                            .foregroundStyle(viewModel.score >= 16 ? .green : .orange)
                        
                        Text("答對 \(viewModel.score) 題 / 總計 20 題")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                    .padding(30)
                    .background(Color.white)
                    .cornerRadius(30)
                    .shadow(color: .black.opacity(0.05), radius: 20)
                    .padding(.top, 40)
                    
                    VStack(alignment: .leading, spacing: 20) {
                        Text("答題詳情")
                            .font(.title3)
                            .fontWeight(.bold)
                            .foregroundStyle(Color(hex: "1e293b"))
                            .padding(.horizontal)
                        
                        ForEach(viewModel.sortedResultsIndices, id: \.self) { index in
                            let q = viewModel.questions[index]
                            let selected = viewModel.selectedAnswers[index]
                            let isCorrect = selected == q.correctIndex
                            
                            ResultRow(index: index, question: q, selected: selected, isCorrect: isCorrect)
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom, 30)
            }
            
            VStack(spacing: 12) {
                Button(action: {
                    withAnimation {
                        viewModel.startQuiz()
                    }
                }) {
                    Text("重新考題")
                        .font(.headline)
                        .foregroundStyle(.indigo)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.white)
                        .cornerRadius(16)
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(Color.indigo, lineWidth: 2)
                        )
                }
                
                Button(action: onReset) {
                    Text("返回主畫面")
                        .font(.headline)
                        .foregroundStyle(.white)
                        .frame(maxWidth: .infinity)
                        .frame(height: 56)
                        .background(Color.indigo)
                        .cornerRadius(16)
                        .shadow(color: .indigo.opacity(0.3), radius: 10, x: 0, y: 5)
                }
            }
            .padding()
            .background(Color.white.opacity(0.8))

        }
    }
}

// 元件區
struct InfoLabel: View {
    var icon: String
    var text: String
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundStyle(.indigo)
                .frame(width: 24)
            Text(text)
                .font(.subheadline)
                .fontWeight(.medium)
                .foregroundStyle(Color(hex: "334155")) // 深灰色文字
        }
    }
}

struct OptionButton: View {
    var text: String
    var isSelected: Bool
    var action: () -> Void
    
    var body: some View {
        Button(action: action) {
            HStack(spacing: 15) {
                Text(text)
                    .multilineTextAlignment(.leading)
                    .font(.system(size: 17, weight: isSelected ? .bold : .regular))
                    .foregroundStyle(isSelected ? .indigo : Color(hex: "334155"))
                    .fixedSize(horizontal: false, vertical: true) // 確保文字超出時往下排
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                ZStack {
                    Circle()
                        .stroke(isSelected ? Color.indigo : Color.gray.opacity(0.3), lineWidth: 2)
                        .frame(width: 24, height: 24)
                    
                    if isSelected {
                        Circle()
                            .fill(Color.indigo)
                            .frame(width: 14, height: 14)
                    }
                }
            }
            .padding(.vertical, 16)
            .padding(.horizontal, 20)
            .background(isSelected ? Color.indigo.opacity(0.08) : Color.white)
            .cornerRadius(16)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(isSelected ? Color.indigo : Color.clear, lineWidth: 2)
            )
            .shadow(color: .black.opacity(isSelected ? 0.08 : 0.03), radius: 8, x: 0, y: 4)
        }
        .buttonStyle(.plain)
    }
}

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: CharacterSet.alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r, g, b: UInt64
        switch hex.count {
        case 3: // RGB (12-bit)
            (r, g, b) = ((int >> 8) * 17, (int >> 4 & 0xF) * 17, (int & 0xF) * 17)
        case 6: // RGB (24-bit)
            (r, g, b) = (int >> 16, int >> 8 & 0xFF, int & 0xFF)
        default:
            (r, g, b) = (1, 1, 0)
        }
        self.init(.sRGB, red: Double(r) / 255, green: Double(g) / 255, blue: Double(b) / 255, opacity: 1)
    }
}

#Preview {
    ContentView()
}

struct ResultRow: View {
    let index: Int
    let question: QuizQuestion
    let selected: Int?
    let isCorrect: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack(alignment: .top, spacing: 12) {
                Text("\(index + 1)")
                    .font(.caption.bold())
                    .foregroundStyle(.white)
                    .frame(width: 24, height: 24)
                    .background(isCorrect ? Color.green : Color.red)
                    .clipShape(Circle())
                
                Text(question.q)
                    .font(.system(size: 16, weight: .medium))
                    .foregroundStyle(Color(hex: "0f172a"))
                    .frame(maxWidth: .infinity, alignment: .leading)
                
                Spacer()
                
                Image(systemName: isCorrect ? "checkmark.circle.fill" : "xmark.circle.fill")
                    .foregroundStyle(isCorrect ? .green : .red)
            }
            
            VStack(alignment: .leading, spacing: 6) {
                HStack(alignment: .top) {
                    Text("您的回答：")
                        .font(.system(size: 14, weight: .bold))
                    Text(selected != nil ? question.shuffledOptions[selected!] : "未作答")
                        .font(.system(size: 14))
                        .fixedSize(horizontal: false, vertical: true)
                }
                .foregroundStyle(isCorrect ? AnyShapeStyle(.secondary) : AnyShapeStyle(Color.red))
                
                HStack(alignment: .top) {
                    Text("正確答案：")
                        .font(.system(size: 14, weight: .bold))
                    Text(question.shuffledOptions[question.correctIndex])
                        .font(.system(size: 14))
                        .fixedSize(horizontal: false, vertical: true)
                }
                .foregroundStyle(.green)
            }
            .padding(.leading, 36)
        }
        .padding()
        .background(Color.white.opacity(0.6))
        .cornerRadius(16)
    }
}
