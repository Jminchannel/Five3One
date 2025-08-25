package com.jmin.five3one.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * 动作教程数据模型
 */
@Entity(tableName = "exercise_tutorials")
@Serializable
data class ExerciseTutorial(
    @PrimaryKey
    val exerciseType: LiftType,
    val videoUrl: String? = null, // 演示视频URL或本地路径
    val gifUrl: String? = null, // GIF动图URL或本地路径
    val commonMistakesVideoUrl: String? = null, // 常见错误演示视频
    val muscleGroupImageUrl: String? = null, // 肌肉群图示
    val difficulty: TutorialDifficulty = TutorialDifficulty.BEGINNER,
    val estimatedDurationMinutes: Int = 5, // 预估学习时长
    val isEnabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 动作教程内容（支持多语言）
 */
@Entity(
    tableName = "exercise_tutorial_content",
    indices = [Index(value = ["exerciseType", "language"])]
)
@Serializable
data class ExerciseTutorialContent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val exerciseType: LiftType,
    val language: Language,
    val title: String, // 动作名称
    val shortDescription: String, // 简短描述
    val keyPoints: List<String>, // 关键要领
    val mentalCues: List<String>, // 心理提示/cues
    val commonMistakes: List<String>, // 常见错误
    val targetMuscles: List<String>, // 目标肌群
    val faqItems: List<FAQItem> = emptyList(), // 常见问答
    val safetyTips: List<String> = emptyList(), // 安全提示
    val createdAt: Long = 0,
    val updatedAt: Long = 0
)

/**
 * FAQ项目
 */
@Serializable
data class FAQItem(
    val question: String,
    val answer: String
)

/**
 * 教程难度等级
 */
@Serializable
enum class TutorialDifficulty {
    BEGINNER,    // 初学者
    INTERMEDIATE, // 中级
    ADVANCED     // 高级
}

/**
 * 教程学习进度
 */
@Entity(tableName = "tutorial_progress")
@Serializable
data class TutorialProgress(
    @PrimaryKey
    val id: String = "", // 格式："{exerciseType}_{language}"
    val exerciseType: LiftType,
    val language: Language,
    val isCompleted: Boolean = false,
    val lastViewedAt: Long = 0L,
    val viewCount: Int = 0,
    val completedAt: Long? = null
) {
    companion object {
        fun generateId(exerciseType: LiftType, language: Language): String {
            return "${exerciseType.name}_${language.code}"
        }
    }
}

/**
 * 预置的教程数据
 */
object TutorialDataPresets {
    /**
     * 获取所有支持的动作类型的教程
     */
    fun getAllTutorials(): List<ExerciseTutorial> {
        return listOf(
            ExerciseTutorial(exerciseType = LiftType.SQUAT),
            ExerciseTutorial(exerciseType = LiftType.BENCH_PRESS),
            ExerciseTutorial(exerciseType = LiftType.DEADLIFT),
            ExerciseTutorial(exerciseType = LiftType.OVERHEAD_PRESS)
        )
    }
    
    /**
     * 获取所有语言的教程内容
     */
    fun getAllTutorialContents(): List<ExerciseTutorialContent> {
        val contents = mutableListOf<ExerciseTutorialContent>()
        
        // 为每种动作和每种语言创建内容
        LiftType.values().forEach { liftType ->
            Language.values().forEach { language ->
                contents.add(createTutorialContent(liftType, language))
            }
        }
        
        return contents
    }
    
    /**
     * 创建特定动作和语言的教程内容
     */
    private fun createTutorialContent(liftType: LiftType, language: Language): ExerciseTutorialContent {
        return when (liftType) {
            LiftType.SQUAT -> createSquatContent(language)
            LiftType.BENCH_PRESS -> createBenchContent(language)
            LiftType.DEADLIFT -> createDeadliftContent(language)
            LiftType.OVERHEAD_PRESS -> createPressContent(language)
        }
    }
    
    /**
     * 深蹲教程内容
     */
    private fun createSquatContent(language: Language): ExerciseTutorialContent {
        return when (language) {
            Language.CHINESE_SIMPLIFIED -> ExerciseTutorialContent(
                exerciseType = LiftType.SQUAT,
                language = language,
                title = "深蹲",
                shortDescription = "下肢力量训练的王者动作，主要锻炼股四头肌、臀大肌和腘绳肌",
                keyPoints = listOf(
                    "站姿：双脚与肩同宽或略宽，脚尖稍朝外",
                    "下蹲：屈髋屈膝，想象向后坐椅子，保持背部挺直",
                    "深度：髋关节低于膝关节（全蹲），或至少大腿与地面平行",
                    "上升：脚跟发力，驱动身体站直，膝盖与脚尖方向一致",
                    "呼吸：下蹲时吸气，起立时呼气"
                ),
                mentalCues = listOf(
                    "想象坐在身后的椅子上",
                    "胸部挺起，眼睛看向前方",
                    "脚跟钉在地面上",
                    "膝盖追随脚尖方向"
                ),
                commonMistakes = listOf(
                    "膝盖内扣（膝外翻）",
                    "上半身过度前倾",
                    "深度不够，髋关节高于膝关节",
                    "脚跟离地",
                    "速度过快，失去控制"
                ),
                targetMuscles = listOf("股四头肌", "臀大肌", "腘绳肌", "小腿肌群", "核心肌群"),
                faqItems = listOf(
                    FAQItem("深蹲时膝盖能超过脚尖吗？", "可以！膝盖适度超过脚尖是正常的，关键是保持膝盖与脚尖方向一致，避免内扣。"),
                    FAQItem("深蹲多深才算标准？", "理想情况下髋关节应低于膝关节，但根据个人柔韧性调整，至少要让大腿与地面平行。")
                ),
                safetyTips = listOf(
                    "开始时使用较轻重量，专注于动作质量",
                    "如有膝盖或髋部不适，请立即停止",
                    "使用深蹲架和安全杠进行保护"
                )
            )
            Language.ENGLISH -> ExerciseTutorialContent(
                exerciseType = LiftType.SQUAT,
                language = language,
                title = "Squat",
                shortDescription = "The king of lower body exercises, primarily targeting quads, glutes, and hamstrings",
                keyPoints = listOf(
                    "Stance: Feet shoulder-width apart or slightly wider, toes slightly turned out",
                    "Descent: Bend hips and knees, imagine sitting back into a chair, keep back straight",
                    "Depth: Hip crease below knee level (full squat), or at least thighs parallel to ground",
                    "Ascent: Drive through heels, stand up straight, knees track over toes",
                    "Breathing: Inhale on the way down, exhale on the way up"
                ),
                mentalCues = listOf(
                    "Sit back into an imaginary chair",
                    "Chest up, eyes looking forward",
                    "Roots through your heels",
                    "Knees track over toes"
                ),
                commonMistakes = listOf(
                    "Knee valgus (knees caving in)",
                    "Excessive forward lean",
                    "Insufficient depth, hips above knees",
                    "Rising up on toes",
                    "Too fast, losing control"
                ),
                targetMuscles = listOf("Quadriceps", "Glutes", "Hamstrings", "Calves", "Core"),
                faqItems = listOf(
                    FAQItem("Can knees go over toes in squat?", "Yes! Knees can moderately track over toes naturally. Key is keeping knees aligned with toes, avoiding inward collapse."),
                    FAQItem("How deep should I squat?", "Ideally hip crease below knee level, but adjust based on mobility. At minimum, thighs parallel to ground.")
                ),
                safetyTips = listOf(
                    "Start with lighter weight, focus on movement quality",
                    "Stop immediately if experiencing knee or hip discomfort",
                    "Use squat rack and safety bars for protection"
                )
            )
            Language.CHINESE_TRADITIONAL -> ExerciseTutorialContent(
                exerciseType = LiftType.SQUAT,
                language = language,
                title = "深蹲",
                shortDescription = "下肢力量訓練的王者動作，主要鍛煉股四頭肌、臀大肌和腘繩肌",
                keyPoints = listOf(
                    "站姿：雙腳與肩同寬或略寬，腳尖稍朝外",
                    "下蹲：屈髖屈膝，想像向後坐椅子，保持背部挺直",
                    "深度：髖關節低於膝關節（全蹲），或至少大腿與地面平行",
                    "上升：腳跟發力，驅動身體站直，膝蓋與腳尖方向一致",
                    "呼吸：下蹲時吸氣，起立時呼氣"
                ),
                mentalCues = listOf(
                    "想像坐在身後的椅子上",
                    "胸部挺起，眼睛看向前方",
                    "腳跟釘在地面上",
                    "膝蓋追隨腳尖方向"
                ),
                commonMistakes = listOf(
                    "膝蓋內扣（膝外翻）",
                    "上半身過度前傾",
                    "深度不夠，髖關節高於膝關節",
                    "腳跟離地",
                    "速度過快，失去控制"
                ),
                targetMuscles = listOf("股四頭肌", "臀大肌", "腘繩肌", "小腿肌群", "核心肌群"),
                faqItems = listOf(
                    FAQItem("深蹲時膝蓋能超過腳尖嗎？", "可以！膝蓋適度超過腳尖是正常的，關鍵是保持膝蓋與腳尖方向一致，避免內扣。"),
                    FAQItem("深蹲多深才算標準？", "理想情況下髖關節應低於膝關節，但根據個人柔韌性調整，至少要讓大腿與地面平行。")
                ),
                safetyTips = listOf(
                    "開始時使用較輕重量，專注於動作質量",
                    "如有膝蓋或髖部不適，請立即停止",
                    "使用深蹲架和安全槓進行保護"
                )
            )
            Language.INDONESIAN -> ExerciseTutorialContent(
                exerciseType = LiftType.SQUAT,
                language = language,
                title = "Squat",
                shortDescription = "Raja latihan tubuh bawah, melatih otot paha depan, glutes, dan hamstring",
                keyPoints = listOf(
                    "Posisi: Kaki selebar bahu atau sedikit lebih lebar, jari kaki sedikit keluar",
                    "Turun: Tekuk pinggul dan lutut, bayangkan duduk di kursi, jaga punggung tetap lurus",
                    "Kedalaman: Pinggul lebih rendah dari lutut, atau minimal paha sejajar lantai",
                    "Naik: Dorong melalui tumit, berdiri tegak, lutut searah dengan jari kaki",
                    "Pernapasan: Tarik napas saat turun, buang napas saat naik"
                ),
                mentalCues = listOf(
                    "Bayangkan duduk di kursi di belakang",
                    "Dada tegak, mata melihat ke depan",
                    "Tumit menempel di lantai",
                    "Lutut mengikuti arah jari kaki"
                ),
                commonMistakes = listOf(
                    "Lutut menekuk ke dalam",
                    "Tubuh terlalu condong ke depan",
                    "Tidak cukup dalam, pinggul di atas lutut",
                    "Tumit terangkat",
                    "Terlalu cepat, kehilangan kontrol"
                ),
                targetMuscles = listOf("Quadriceps", "Glutes", "Hamstring", "Betis", "Core"),
                faqItems = listOf(
                    FAQItem("Bolehkah lutut melewati jari kaki saat squat?", "Boleh! Lutut yang sedikit melewati jari kaki adalah normal. Yang penting adalah menjaga lutut searah dengan jari kaki."),
                    FAQItem("Seberapa dalam squat yang benar?", "Idealnya pinggul lebih rendah dari lutut, tapi sesuaikan dengan fleksibilitas. Minimal paha sejajar dengan lantai.")
                ),
                safetyTips = listOf(
                    "Mulai dengan beban ringan, fokus pada kualitas gerakan",
                    "Berhenti segera jika ada ketidaknyamanan di lutut atau pinggul",
                    "Gunakan squat rack dan safety bar untuk perlindungan"
                )
            )
        }
    }
    
    /**
     * 卧推教程内容
     */
    private fun createBenchContent(language: Language): ExerciseTutorialContent {
        return when (language) {
            Language.CHINESE_SIMPLIFIED -> ExerciseTutorialContent(
                exerciseType = LiftType.BENCH_PRESS,
                language = language,
                title = "卧推",
                shortDescription = "上肢力量训练的经典动作，主要锻炼胸大肌、前三角肌和肱三头肌",
                keyPoints = listOf(
                    "躺位：肩胛骨收紧，背部轻微拱起，双脚平放地面",
                    "握距：略宽于肩膀，双手对称握杠",
                    "下放：控制杠铃缓慢下降至胸部，轻触胸部",
                    "上推：垂直向上推起，肘部不完全锁死",
                    "呼吸：下降时吸气，上推时呼气"
                ),
                mentalCues = listOf(
                    "想象把地板推开",
                    "肘部沿着身体两侧运动",
                    "保持肩胛骨稳定",
                    "胸部向上顶"
                ),
                commonMistakes = listOf(
                    "肩胛骨没有收紧",
                    "肘部张得过开（90度角）",
                    "杠铃弹跳胸部",
                    "下背部拱得过度",
                    "握距不对称"
                ),
                targetMuscles = listOf("胸大肌", "前三角肌", "肱三头肌", "前锯肌"),
                faqItems = listOf(
                    FAQItem("卧推时肘部要夹多紧？", "肘部应该在45度角左右，不要完全贴身也不要完全张开，找到最舒服的角度。"),
                    FAQItem("杠铃要完全触胸吗？", "轻触胸部即可，不要用力弹跳，这样可以保护肩关节并确保全程发力。")
                ),
                safetyTips = listOf(
                    "始终使用安全杠或有人协助",
                    "不要独自进行大重量卧推",
                    "感到不适时立即放下重量"
                )
            )
            Language.ENGLISH -> ExerciseTutorialContent(
                exerciseType = LiftType.BENCH_PRESS,
                language = language,
                title = "Bench Press",
                shortDescription = "Classic upper body exercise targeting chest, front delts, and triceps",
                keyPoints = listOf(
                    "Setup: Retract shoulder blades, slight arch in back, feet flat on floor",
                    "Grip: Slightly wider than shoulders, symmetric hand position",
                    "Descent: Control bar slowly to chest, lightly touch chest",
                    "Press: Push straight up, don't fully lock elbows",
                    "Breathing: Inhale on descent, exhale on press"
                ),
                mentalCues = listOf(
                    "Imagine pushing the floor away",
                    "Elbows move along your sides",
                    "Keep shoulder blades stable",
                    "Drive chest up"
                ),
                commonMistakes = listOf(
                    "Shoulder blades not retracted",
                    "Elbows too wide (90-degree angle)",
                    "Bouncing bar off chest",
                    "Excessive back arch",
                    "Asymmetric grip"
                ),
                targetMuscles = listOf("Pectorals", "Front Deltoids", "Triceps", "Serratus Anterior"),
                faqItems = listOf(
                    FAQItem("How tight should elbows be in bench press?", "Elbows should be around 45 degrees, not tucked tight or flared wide. Find your comfortable angle."),
                    FAQItem("Should the bar fully touch the chest?", "Lightly touch the chest without bouncing. This protects shoulders and ensures full range of motion.")
                ),
                safetyTips = listOf(
                    "Always use safety bars or have a spotter",
                    "Never bench heavy weight alone",
                    "Drop the weight immediately if feeling discomfort"
                )
            )
            Language.CHINESE_TRADITIONAL -> ExerciseTutorialContent(
                exerciseType = LiftType.BENCH_PRESS,
                language = language,
                title = "臥推",
                shortDescription = "上肢力量訓練的經典動作，主要鍛煉胸大肌、前三角肌和肱三頭肌",
                keyPoints = listOf(
                    "躺位：肩胛骨收緊，背部輕微拱起，雙腳平放地面",
                    "握距：略寬於肩膀，雙手對稱握槓",
                    "下放：控制槓鈴緩慢下降至胸部，輕觸胸部",
                    "上推：垂直向上推起，肘部不完全鎖死",
                    "呼吸：下降時吸氣，上推時呼氣"
                ),
                mentalCues = listOf(
                    "想像把地板推開",
                    "肘部沿著身體兩側運動",
                    "保持肩胛骨穩定",
                    "胸部向上頂"
                ),
                commonMistakes = listOf(
                    "肩胛骨沒有收緊",
                    "肘部張得過開（90度角）",
                    "槓鈴彈跳胸部",
                    "下背部拱得過度",
                    "握距不對稱"
                ),
                targetMuscles = listOf("胸大肌", "前三角肌", "肱三頭肌", "前鋸肌"),
                faqItems = listOf(
                    FAQItem("臥推時肘部要夾多緊？", "肘部應該在45度角左右，不要完全貼身也不要完全張開，找到最舒服的角度。"),
                    FAQItem("槓鈴要完全觸胸嗎？", "輕觸胸部即可，不要用力彈跳，這樣可以保護肩關節並確保全程發力。")
                ),
                safetyTips = listOf(
                    "始終使用安全槓或有人協助",
                    "不要獨自進行大重量臥推",
                    "感到不適時立即放下重量"
                )
            )
            Language.INDONESIAN -> ExerciseTutorialContent(
                exerciseType = LiftType.BENCH_PRESS,
                language = language,
                title = "Bench Press",
                shortDescription = "Latihan klasik tubuh atas untuk melatih otot dada, deltoid depan, dan triceps",
                keyPoints = listOf(
                    "Posisi: Tarik tulang belikat, punggung sedikit melengkung, kaki rata di lantai",
                    "Genggaman: Sedikit lebih lebar dari bahu, posisi tangan simetris",
                    "Turunkan: Kontrol barbel perlahan ke dada, sentuh ringan dada",
                    "Dorong: Dorong lurus ke atas, jangan kunci siku penuh",
                    "Pernapasan: Tarik napas saat turun, buang napas saat dorong"
                ),
                mentalCues = listOf(
                    "Bayangkan mendorong lantai menjauh",
                    "Siku bergerak di sepanjang sisi tubuh",
                    "Jaga tulang belikat stabil",
                    "Dorong dada ke atas"
                ),
                commonMistakes = listOf(
                    "Tulang belikat tidak tertarik",
                    "Siku terlalu lebar (sudut 90 derajat)",
                    "Memantulkan barbel di dada",
                    "Punggung melengkung berlebihan",
                    "Genggaman tidak simetris"
                ),
                targetMuscles = listOf("Pectorals", "Deltoid Depan", "Triceps", "Serratus Anterior"),
                faqItems = listOf(
                    FAQItem("Seberapa rapat siku saat bench press?", "Siku sekitar 45 derajat, tidak terlalu rapat atau terlalu lebar. Temukan sudut yang nyaman."),
                    FAQItem("Apakah barbel harus menyentuh dada penuh?", "Sentuh dada ringan tanpa memantul. Ini melindungi bahu dan memastikan range of motion penuh.")
                ),
                safetyTips = listOf(
                    "Selalu gunakan safety bar atau spotter",
                    "Jangan bench press berat sendirian",
                    "Lepas beban segera jika merasa tidak nyaman"
                )
            )
        }
    }
    
    /**
     * 硬拉教程内容
     */
    private fun createDeadliftContent(language: Language): ExerciseTutorialContent {
        return when (language) {
            Language.CHINESE_SIMPLIFIED -> ExerciseTutorialContent(
                exerciseType = LiftType.DEADLIFT,
                language = language,
                title = "硬拉",
                shortDescription = "全身性复合动作，主要锻炼后链肌群：腘绳肌、臀大肌和竖脊肌",
                keyPoints = listOf(
                    "起始位置：杠铃贴近小腿，双脚与髋同宽",
                    "握距：双手在腿外侧，肩胛骨在杠铃正上方",
                    "背部：保持脊柱中立，胸部挺起",
                    "起拉：同时伸髋伸膝，杠铃贴着身体向上拉",
                    "锁定：站直时髋关节完全伸展，肩膀在髋部正上方"
                ),
                mentalCues = listOf(
                    "把地板推开",
                    "杠铃杆贴着小腿向上拉",
                    "想象夹紧腋下的毛巾",
                    "髋部向前顶"
                ),
                commonMistakes = listOf(
                    "起始位置杠铃离身体太远",
                    "背部圆背",
                    "膝盖过早锁定",
                    "杠铃离开身体轨迹",
                    "肩膀在杠铃前方"
                ),
                targetMuscles = listOf("腘绳肌", "臀大肌", "竖脊肌", "斜方肌", "前臂"),
                faqItems = listOf(
                    FAQItem("硬拉时腰部疼痛怎么办？", "立即停止训练。疼痛可能由圆背或重量过大引起。检查动作技术，减少重量。"),
                    FAQItem("应该使用助力带吗？", "初学阶段建议不用，先发展握力。当握力成为限制因素时再考虑使用。")
                ),
                safetyTips = listOf(
                    "从较轻重量开始，专注技术",
                    "保持脊柱中立，避免圆背",
                    "感到腰部不适立即停止"
                )
            )
            Language.ENGLISH -> ExerciseTutorialContent(
                exerciseType = LiftType.DEADLIFT,
                language = language,
                title = "Deadlift",
                shortDescription = "Full-body compound movement targeting posterior chain: hamstrings, glutes, and erector spinae",
                keyPoints = listOf(
                    "Setup: Bar close to shins, feet hip-width apart",
                    "Grip: Hands outside legs, shoulder blades over the bar",
                    "Back: Neutral spine, chest up",
                    "Lift: Extend hips and knees together, bar stays close to body",
                    "Lockout: Stand tall with hips fully extended, shoulders over hips"
                ),
                mentalCues = listOf(
                    "Push the floor away",
                    "Drag the bar up your shins",
                    "Imagine squeezing a towel under your armpits",
                    "Drive hips forward"
                ),
                commonMistakes = listOf(
                    "Bar too far from body at start",
                    "Rounded back",
                    "Knees lock too early",
                    "Bar drifts away from body",
                    "Shoulders in front of the bar"
                ),
                targetMuscles = listOf("Hamstrings", "Glutes", "Erector Spinae", "Traps", "Forearms"),
                faqItems = listOf(
                    FAQItem("What if my lower back hurts during deadlifts?", "Stop immediately. Pain may be from rounding or excessive weight. Check technique, reduce weight."),
                    FAQItem("Should I use lifting straps?", "Not initially - develop grip strength first. Use when grip becomes the limiting factor.")
                ),
                safetyTips = listOf(
                    "Start with lighter weight, focus on technique",
                    "Maintain neutral spine, avoid rounding",
                    "Stop immediately if feeling lower back discomfort"
                )
            )
            Language.CHINESE_TRADITIONAL -> ExerciseTutorialContent(
                exerciseType = LiftType.DEADLIFT,
                language = language,
                title = "硬拉",
                shortDescription = "全身性複合動作，主要鍛煉後鏈肌群：腘繩肌、臀大肌和豎脊肌",
                keyPoints = listOf(
                    "起始位置：槓鈴貼近小腿，雙腳與髖同寬",
                    "握距：雙手在腿外側，肩胛骨在槓鈴正上方",
                    "背部：保持脊柱中立，胸部挺起",
                    "起拉：同時伸髖伸膝，槓鈴貼著身體向上拉",
                    "鎖定：站直時髖關節完全伸展，肩膀在髖部正上方"
                ),
                mentalCues = listOf(
                    "把地板推開",
                    "槓鈴桿貼著小腿向上拉",
                    "想像夾緊腋下的毛巾",
                    "髖部向前頂"
                ),
                commonMistakes = listOf(
                    "起始位置槓鈴離身體太遠",
                    "背部圓背",
                    "膝蓋過早鎖定",
                    "槓鈴離開身體軌跡",
                    "肩膀在槓鈴前方"
                ),
                targetMuscles = listOf("腘繩肌", "臀大肌", "豎脊肌", "斜方肌", "前臂"),
                faqItems = listOf(
                    FAQItem("硬拉時腰部疼痛怎麼辦？", "立即停止訓練。疼痛可能由圓背或重量過大引起。檢查動作技術，減少重量。"),
                    FAQItem("應該使用助力帶嗎？", "初學階段建議不用，先發展握力。當握力成為限制因素時再考慮使用。")
                ),
                safetyTips = listOf(
                    "從較輕重量開始，專注技術",
                    "保持脊柱中立，避免圓背",
                    "感到腰部不適立即停止"
                )
            )
            Language.INDONESIAN -> ExerciseTutorialContent(
                exerciseType = LiftType.DEADLIFT,
                language = language,
                title = "Deadlift",
                shortDescription = "Gerakan compound seluruh tubuh untuk posterior chain: hamstring, glutes, dan erector spinae",
                keyPoints = listOf(
                    "Posisi awal: Barbel dekat dengan tulang kering, kaki selebar pinggul",
                    "Genggaman: Tangan di luar kaki, tulang belikat di atas barbel",
                    "Punggung: Tulang belakang netral, dada tegak",
                    "Angkat: Ekstensi pinggul dan lutut bersamaan, barbel dekat tubuh",
                    "Lockout: Berdiri tegak dengan pinggul ekstensi penuh, bahu di atas pinggul"
                ),
                mentalCues = listOf(
                    "Dorong lantai menjauh",
                    "Seret barbel di sepanjang tulang kering",
                    "Bayangkan menjepit handuk di ketiak",
                    "Dorong pinggul ke depan"
                ),
                commonMistakes = listOf(
                    "Barbel terlalu jauh dari tubuh di awal",
                    "Punggung membulat",
                    "Lutut mengunci terlalu cepat",
                    "Barbel menjauh dari tubuh",
                    "Bahu di depan barbel"
                ),
                targetMuscles = listOf("Hamstring", "Glutes", "Erector Spinae", "Trapezius", "Lengan bawah"),
                faqItems = listOf(
                    FAQItem("Bagaimana jika punggung bawah sakit saat deadlift?", "Hentikan segera. Nyeri mungkin dari punggung membulat atau beban berlebih. Periksa teknik, kurangi beban."),
                    FAQItem("Haruskah menggunakan lifting strap?", "Tidak di awal - kembangkan kekuatan genggaman dulu. Gunakan saat genggaman menjadi faktor pembatas.")
                ),
                safetyTips = listOf(
                    "Mulai dengan beban ringan, fokus pada teknik",
                    "Pertahankan tulang belakang netral, hindari membulat",
                    "Hentikan segera jika merasa tidak nyaman di punggung bawah"
                )
            )
        }
    }
    
    /**
     * 推举教程内容
     */
    private fun createPressContent(language: Language): ExerciseTutorialContent {
        return when (language) {
            Language.CHINESE_SIMPLIFIED -> ExerciseTutorialContent(
                exerciseType = LiftType.OVERHEAD_PRESS,
                language = language,
                title = "推举",
                shortDescription = "上肢垂直推举动作，主要锻炼肩部、肱三头肌和核心稳定性",
                keyPoints = listOf(
                    "起始位置：杠铃放在上胸/锁骨位置，肘部在杠铃下方",
                    "核心收紧：绷紧腹部和臀部，保持身体稳定",
                    "推举路径：垂直向上推，杠铃经过头部前方",
                    "锁定：手臂伸直，杠铃在头部正上方",
                    "下放：控制杠铃回到起始位置"
                ),
                mentalCues = listOf(
                    "想象把天花板推开",
                    "肘部向前，不要向两侧张开",
                    "核心像穿了紧身胸衣",
                    "杠铃走直线向上"
                ),
                commonMistakes = listOf(
                    "肘部张得太开",
                    "核心松懈，身体摇摆",
                    "杠铃向前推而非向上",
                    "锁定时杠铃在头前方而非正上方",
                    "下背部过度拱起"
                ),
                targetMuscles = listOf("三角肌", "肱三头肌", "上胸肌", "核心肌群", "前锯肌"),
                faqItems = listOf(
                    FAQItem("推举时为什么肩膀疼？", "可能是技术问题或柔韧性不足。检查肘部位置，确保充分热身，必要时降低重量。"),
                    FAQItem("可以坐着推举吗？", "可以，但站姿推举能更好地训练核心稳定性和全身协调。")
                ),
                safetyTips = listOf(
                    "充分热身肩关节",
                    "从轻重量开始练习技术",
                    "如有肩部不适立即停止"
                )
            )
            Language.ENGLISH -> ExerciseTutorialContent(
                exerciseType = LiftType.OVERHEAD_PRESS,
                language = language,
                title = "Overhead Press",
                shortDescription = "Upper body vertical pressing movement targeting shoulders, triceps, and core stability",
                keyPoints = listOf(
                    "Setup: Bar rests on upper chest/collarbone, elbows under the bar",
                    "Core tight: Brace abs and glutes, maintain body stability",
                    "Press path: Straight up, bar travels in front of face",
                    "Lockout: Arms extended, bar directly overhead",
                    "Descent: Control bar back to starting position"
                ),
                mentalCues = listOf(
                    "Imagine pushing the ceiling away",
                    "Elbows forward, not flared out",
                    "Core like wearing a tight corset",
                    "Bar travels straight up"
                ),
                commonMistakes = listOf(
                    "Elbows too wide",
                    "Loose core, body swaying",
                    "Pressing forward instead of up",
                    "Bar in front of head at lockout instead of overhead",
                    "Excessive back arch"
                ),
                targetMuscles = listOf("Deltoids", "Triceps", "Upper Chest", "Core", "Serratus Anterior"),
                faqItems = listOf(
                    FAQItem("Why do my shoulders hurt during press?", "Could be technique issues or mobility limitations. Check elbow position, ensure proper warm-up, reduce weight if needed."),
                    FAQItem("Can I press seated?", "Yes, but standing press better develops core stability and full-body coordination.")
                ),
                safetyTips = listOf(
                    "Warm up shoulders thoroughly",
                    "Start with light weight to practice technique",
                    "Stop immediately if experiencing shoulder discomfort"
                )
            )
            Language.CHINESE_TRADITIONAL -> ExerciseTutorialContent(
                exerciseType = LiftType.OVERHEAD_PRESS,
                language = language,
                title = "推舉",
                shortDescription = "上肢垂直推舉動作，主要鍛煉肩部、肱三頭肌和核心穩定性",
                keyPoints = listOf(
                    "起始位置：槓鈴放在上胸/鎖骨位置，肘部在槓鈴下方",
                    "核心收緊：繃緊腹部和臀部，保持身體穩定",
                    "推舉路徑：垂直向上推，槓鈴經過頭部前方",
                    "鎖定：手臂伸直，槓鈴在頭部正上方",
                    "下放：控制槓鈴回到起始位置"
                ),
                mentalCues = listOf(
                    "想像把天花板推開",
                    "肘部向前，不要向兩側張開",
                    "核心像穿了緊身胸衣",
                    "槓鈴走直線向上"
                ),
                commonMistakes = listOf(
                    "肘部張得太開",
                    "核心鬆懈，身體搖擺",
                    "槓鈴向前推而非向上",
                    "鎖定時槓鈴在頭前方而非正上方",
                    "下背部過度拱起"
                ),
                targetMuscles = listOf("三角肌", "肱三頭肌", "上胸肌", "核心肌群", "前鋸肌"),
                faqItems = listOf(
                    FAQItem("推舉時為什麼肩膀疼？", "可能是技術問題或柔韌性不足。檢查肘部位置，確保充分熱身，必要時降低重量。"),
                    FAQItem("可以坐著推舉嗎？", "可以，但站姿推舉能更好地訓練核心穩定性和全身協調。")
                ),
                safetyTips = listOf(
                    "充分熱身肩關節",
                    "從輕重量開始練習技術",
                    "如有肩部不適立即停止"
                )
            )
            Language.INDONESIAN -> ExerciseTutorialContent(
                exerciseType = LiftType.OVERHEAD_PRESS,
                language = language,
                title = "Overhead Press",
                shortDescription = "Gerakan pressing vertikal tubuh atas untuk bahu, triceps, dan stabilitas core",
                keyPoints = listOf(
                    "Posisi awal: Barbel di dada atas/tulang selangka, siku di bawah barbel",
                    "Core kencang: Kencangkan perut dan glutes, jaga stabilitas tubuh",
                    "Jalur press: Lurus ke atas, barbel melewati depan wajah",
                    "Lockout: Lengan ekstensi, barbel tepat di atas kepala",
                    "Turun: Kontrol barbel kembali ke posisi awal"
                ),
                mentalCues = listOf(
                    "Bayangkan mendorong langit-langit",
                    "Siku ke depan, tidak melebar",
                    "Core seperti memakai korset ketat",
                    "Barbel bergerak lurus ke atas"
                ),
                commonMistakes = listOf(
                    "Siku terlalu lebar",
                    "Core kendor, tubuh goyang",
                    "Mendorong ke depan bukan ke atas",
                    "Barbel di depan kepala saat lockout bukan di atas",
                    "Punggung melengkung berlebihan"
                ),
                targetMuscles = listOf("Deltoid", "Triceps", "Dada Atas", "Core", "Serratus Anterior"),
                faqItems = listOf(
                    FAQItem("Mengapa bahu sakit saat press?", "Mungkin masalah teknik atau keterbatasan mobilitas. Periksa posisi siku, pastikan pemanasan cukup, kurangi beban jika perlu."),
                    FAQItem("Boleh press sambil duduk?", "Boleh, tapi standing press lebih mengembangkan stabilitas core dan koordinasi seluruh tubuh.")
                ),
                safetyTips = listOf(
                    "Pemanasan bahu dengan baik",
                    "Mulai dengan beban ringan untuk latihan teknik",
                    "Hentikan segera jika merasa tidak nyaman di bahu"
                )
            )
        }
    }
}

/**
 * 完整的教程信息数据类
 */
data class CompleteTutorial(
    val tutorial: ExerciseTutorial,
    val content: ExerciseTutorialContent,
    val progress: TutorialProgress?
)
