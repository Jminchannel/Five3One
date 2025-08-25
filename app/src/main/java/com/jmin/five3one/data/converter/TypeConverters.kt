package com.jmin.five3one.data.converter

import androidx.room.TypeConverter
import com.jmin.five3one.data.model.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room数据库类型转换器
 */
class TypeConverters {
    
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }
    
    // LiftType转换
    @TypeConverter
    fun fromLiftType(liftType: LiftType): String = liftType.name
    
    @TypeConverter
    fun toLiftType(liftTypeName: String): LiftType = 
        LiftType.valueOf(liftTypeName)
    
    // TemplateType转换
    @TypeConverter
    fun fromTemplateType(templateType: TemplateType): String = templateType.id
    
    @TypeConverter
    fun toTemplateType(templateId: String): TemplateType = 
        TemplateType.values().find { it.id == templateId } ?: TemplateType.FIVES
    
    // Language转换
    @TypeConverter
    fun fromLanguage(language: Language): String = language.code
    
    @TypeConverter
    fun toLanguage(languageCode: String): Language = 
        Language.fromCode(languageCode)
    
    // WorkoutFeeling转换
    @TypeConverter
    fun fromWorkoutFeeling(feeling: WorkoutFeeling): String = feeling.name
    
    @TypeConverter
    fun toWorkoutFeeling(feelingName: String): WorkoutFeeling = 
        WorkoutFeeling.valueOf(feelingName)
    
    // List<Double>转换（杠铃片列表）
    @TypeConverter
    fun fromDoubleList(value: List<Double>): String = 
        json.encodeToString(value)
    
    @TypeConverter
    fun toDoubleList(value: String): List<Double> = 
        json.decodeFromString(value)
    
    // List<WorkoutSet>转换
    @TypeConverter
    fun fromWorkoutSetList(value: List<WorkoutSet>): String = 
        json.encodeToString(value)
    
    @TypeConverter
    fun toWorkoutSetList(value: String): List<WorkoutSet> = 
        json.decodeFromString(value)
    
    // List<String>转换（教程相关列表）
    @TypeConverter
    fun fromStringList(value: List<String>): String = 
        json.encodeToString(value)
    
    @TypeConverter
    fun toStringList(value: String): List<String> = 
        json.decodeFromString(value)
    
    // List<FAQItem>转换
    @TypeConverter
    fun fromFAQItemList(value: List<FAQItem>): String = 
        json.encodeToString(value)
    
    @TypeConverter
    fun toFAQItemList(value: String): List<FAQItem> = 
        json.decodeFromString(value)
    
    // TutorialDifficulty转换
    @TypeConverter
    fun fromTutorialDifficulty(difficulty: TutorialDifficulty): String = difficulty.name
    
    @TypeConverter
    fun toTutorialDifficulty(difficultyName: String): TutorialDifficulty = 
        TutorialDifficulty.valueOf(difficultyName)
}
