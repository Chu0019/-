import SwiftUI

class QuizViewModel: ObservableObject {
    @Published var questions: [Question] = []
    @Published var currentIndex = 0
    @Published var selectedAnswers: [Int?] = []
    @Published var showResults = false
    @Published var score = 0
    
    func startQuiz() {
        // Pick 20 random questions
        let shuffled = questionBank.shuffled()
        self.questions = Array(shuffled.prefix(min(20, shuffled.count)))
        self.selectedAnswers = Array(repeating: nil, count: self.questions.count)
        self.currentIndex = 0
        self.showResults = false
        self.score = 0
    }
    
    func submit() {
        var calculatedScore = 0
        for (index, q) in questions.enumerated() {
            if selectedAnswers[index] == q.answer {
                calculatedScore += 1
            }
        }
        self.score = calculatedScore
        self.showResults = true
    }
}

struct ContentView: View {
    @StateObject private var viewModel = QuizViewModel()
    @State private var quizStarted = false
    
    var body: some View {
        ZStack {
            // Background Gradient
            LinearGradient(gradient: Gradient(colors: [Color(hex: "e0e7ff"), Color(hex: "f8fafc")]), 
                           startPoint: .topLeading, 
                           endPoint: .bottomTrailing)
                .ignoresSafeArea()
            
            if !quizStarted {
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
                    .foregroundStyle(.secondary)
                Text("空側駕駛許可證測驗")
                    .font(.system(size: 34, weight: .black))
                    .multilineTextAlignment(.center)
                    .lineLimit(2)
            }
            .padding(.horizontal)
            
            VStack(alignment: .leading, spacing: 15) {
                InfoLabel(icon: "list.bullet.clipboard", text: "題庫：共 \(questionBank.count) 題")
                InfoLabel(icon: "timer", text: "每次隨機抽取 20 題")
                InfoLabel(icon: "checkmark.seal", text: "模擬真實測驗環境 (80分及格)")
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
            // Header
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
                        .font(.title3)
                        .fontWeight(.bold)
                        .lineSpacing(4)
                    
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
                        ForEach(0..<currentQ.options.count, id: \.self) { index in
                            let option = currentQ.options[index]
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
            
            // Footer
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
                                Text("分")
                                    .font(.headline)
                                    .foregroundStyle(.secondary)
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
                            .padding(.horizontal)
                        
                        ForEach(0..<viewModel.questions.count, id: \.self) { index in
                            let q = viewModel.questions[index]
                            let selected = viewModel.selectedAnswers[index]
                            let isCorrect = selected == q.answer
                            
                            VStack(alignment: .leading, spacing: 12) {
                                HStack(alignment: .top, spacing: 12) {
                                    Text("\(index + 1)")
                                        .font(.caption.bold())
                                        .foregroundStyle(.white)
                                        .frame(width: 24, height: 24)
                                        .background(isCorrect ? Color.green : Color.red)
                                        .clipShape(Circle())
                                    
                                    Text(q.q)
                                        .font(.subheadline)
                                        .fontWeight(.medium)
                                    
                                    Spacer()
                                    
                                    Image(systemName: isCorrect ? "checkmark.circle.fill" : "xmark.circle.fill")
                                        .foregroundStyle(isCorrect ? .green : .red)
                                }
                                
                                if !isCorrect {
                                    VStack(alignment: .leading, spacing: 6) {
                                        HStack {
                                            Text("您的回答：")
                                                .font(.caption.bold())
                                            Text(selected != nil ? q.options[selected!] : "未作答")
                                                .font(.caption)
                                        }
                                        .foregroundStyle(.red)
                                        
                                        HStack {
                                            Text("正確答案：")
                                                .font(.caption.bold())
                                            Text(q.options[q.answer])
                                                .font(.caption)
                                        }
                                        .foregroundStyle(.green)
                                    }
                                    .padding(.leading, 36)
                                }
                            }
                            .padding()
                            .background(Color.white.opacity(0.6))
                            .cornerRadius(16)
                        }
                    }
                    .padding(.horizontal)
                }
                .padding(.bottom, 30)
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
            .padding()
            .background(Color.white.opacity(0.8))
        }
    }
}

// Reusable Components
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
                .foregroundStyle(.primary.opacity(0.8))
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
                    .font(.body)
                    .fontWeight(isSelected ? .bold : .regular)
                    .foregroundStyle(isSelected ? .indigo : .primary)
                
                Spacer()
                
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
