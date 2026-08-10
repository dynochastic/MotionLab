# Lesson Progress Flow: Pre-Test to Post-Test

## Overview
This document explains the complete flow of lesson progress from pre-test to post-test, including how lessons unlock, subtopics progress, and completion tracking.

---

## 1. Initial State

### Lesson Unlocking
- **First Lesson (Lesson 1)**: Automatically unlocked for all users
- **Subsequent Lessons**: Locked by default, unlocked when previous lesson's post-test is passed (score ≥ 8)
- **Unlock Status**: Stored in `LessonProgressEntity.isUnlocked`

### Pre-Test State
- **Status**: Available (not locked) for unlocked lessons
- **Visual**: Pre-test button is active (not greyed out)
- **Can Take**: Yes, before accessing subtopics

---

## 2. Pre-Test Flow

### Taking Pre-Test
1. User navigates to lesson content screen
2. Pre-test button is visible and clickable (if lesson is unlocked)
3. User clicks pre-test → navigates to Test Rules screen
4. User proceeds to test screen (30-minute timer starts)
5. User completes test (15 multiple-choice questions)
6. Test is submitted (manually or auto-submit on timeout)

### Pre-Test Scoring
- **Passing Score**: ≥ 6 out of 15
- **First Attempt Only**: Only the first pre-test score is stored in `preTestScore`
- **Multiple Attempts**: All attempts are recorded in `test_attempts` table, but progress only stores first score
- **After First Attempt**: Pre-test button becomes greyed out (`isGreyedOut = true`) and cannot be retaken

### Pre-Test Progress Update
When pre-test is submitted:
```kotlin
// In LessonRepository.updateLessonProgressAfterTest()
if (isPreTest) {
    if (progress == null) {
        // First pre-test ever - create new progress
        newProgress = LessonProgressEntity(
            username = username,
            lessonId = lessonId,
            preTestTaken = true,
            preTestScore = score,
            isUnlocked = true  // Unlock lesson
        )
    } else if (!progress.preTestTaken) {
        // First pre-test for this lesson
        updated = progress.copy(
            preTestTaken = true,
            preTestScore = score,
            isUnlocked = true
        )
    } else {
        // Pre-test already taken - no score update, just ensure unlocked
        // Pre-test button is greyed out in UI
    }
}
```

### Achievements
- **First Pre-Test Attempt**: Unlocks lesson-specific achievements
  - Lesson 1: "Quick Thinker"
  - Lesson 2: "Law Learner"
  - Lesson 3: "Power Prepper"

---

## 3. Subtopics Flow

### Subtopic Unlocking
1. **First Subtopic**: Unlocked immediately after pre-test is taken (`preTestTaken = true`)
2. **Subsequent Subtopics**: Unlocked when previous subtopic is fully completed
   - Requires: `videoCompleted = true` AND `problemCompleted = true` AND `simulationCompleted = true`

### Subtopic Completion Logic
```kotlin
// In SubtopicViewModel.initializeSubtopics()
val isUnlocked = when {
    index == 0 -> lessonPreTestTaken  // First subtopic requires pre-test
    else -> {
        val prevProgress = progresses.find { it.subtopicId == prev?.subtopicId }
        prevProgress?.videoCompleted == true && 
        prevProgress.problemCompleted == true && 
        prevProgress.simulationCompleted == true
    }
}
```

### Subtopic Materials
Each subtopic has 3 materials that must be completed:
1. **Video**: Marked complete when video is watched
2. **Problem**: Marked complete when problem/exercise is solved
3. **Simulation**: Marked complete when Unity simulation is completed

### Progress Tracking
- Stored in `SubtopicProgressEntity`
- Fields: `videoCompleted`, `problemCompleted`, `simulationCompleted`
- When all 3 are complete, next subtopic is automatically unlocked

---

## 4. Post-Test Flow

### Post-Test Availability
Post-test is **locked** until:
1. Pre-test is taken (`preTestTaken = true`)
2. **ALL** subtopics are completed (all video, problem, simulation done)

### Post-Test Lock Logic
```kotlin
// In SubtopicScreen.kt
val isPostTestLocked = !(
    progress?.preTestTaken == true && 
    subtopics.all { 
        it.videoCompleted && 
        it.problemCompleted && 
        it.simulationCompleted 
    }
)
```

### Taking Post-Test
1. User completes all subtopics
2. Post-test button becomes unlocked
3. User navigates to Test Rules screen
4. User proceeds to test screen (25-minute timer starts)
5. User completes test (15 multiple-choice questions)
6. Test is submitted

### Post-Test Scoring
- **Passing Score**: ≥ 8 out of 15
- **Perfect Score**: 15/15 (unlocks special achievements)
- **Multiple Attempts**: Allowed - user can retake to improve score
- **Best Score Tracking**: System tracks highest score and best time for that score

### Post-Test Progress Update
When post-test is submitted:
```kotlin
// In LessonRepository.updateLessonProgressAfterTest()
if (!isPreTest) {
    val isFirstPostTest = progress == null || firstPostTestScore == null
    
    if (progress == null || bestScore == null || score > bestScore) {
        // New best score
        updated = progress.copy(
            postTestScore = score,              // Best score
            postTestTime = timeTaken,           // Time for best score
            firstPostTestScore = if (isFirstPostTest) score else firstPostTestScore,
            firstPostTestTime = if (isFirstPostTest) timeTaken else firstPostTestTime,
            isUnlocked = true
        )
    } else if (score == bestScore && timeTaken < bestTime) {
        // Tied score, better time
        updated = progress.copy(
            postTestTime = timeTaken
        )
    }
}
```

### Dual Score Tracking
The system tracks **two sets** of post-test scores:
1. **First Post-Test Values** (`firstPostTestScore`, `firstPostTestTime`)
   - Recorded only on first attempt
   - **Never updated** after first attempt
   - Used for leaderboards
   
2. **Best Post-Test Values** (`postTestScore`, `postTestTime`)
   - Updated whenever a better score is achieved
   - Used for current performance tracking

### Lesson Completion
- **Fully Completed**: `isLessonFullyCompleted = true` when post-test score ≥ 8
- **Next Lesson Unlock**: Automatically unlocks next lesson when score ≥ 8
- **Achievements**: Unlocks lesson completion achievements
  - Lesson 1: "Motion Seeker" (all subtopics + post-test)
  - Lesson 2: "Force Follower"
  - Lesson 3: "Energy Chaser"

### Post-Test Achievements
- **Perfect Score (15/15)**: Unlocks special achievements
  - Lesson 1: "Motion Master"
  - Lesson 2: "Law Breaker"
  - Lesson 3: "Energy Overload"

---

## 5. Next Lesson Unlock

### Unlock Logic
```kotlin
// In LessonViewModel.onPostTestPassed()
if (completed) {  // score >= 8
    lessonRepository.unlockNextLesson(lessonId, username)
}
```

### Unlock Process
1. Post-test score ≥ 8 triggers unlock
2. Next lesson's `isUnlocked` is set to `true`
3. First subtopic of next lesson is automatically unlocked
4. User can now access the next lesson

---

## 6. Data Flow Summary

### LessonProgressEntity Fields
| Field | Description | When Set |
|-------|-------------|----------|
| `preTestTaken` | Flag indicating pre-test was taken | After first pre-test submission |
| `preTestScore` | First pre-test score | After first pre-test submission (never updated) |
| `postTestScore` | Best post-test score | Updated when better score achieved |
| `postTestTime` | Time for best post-test score | Updated with best score |
| `firstPostTestScore` | First post-test score (leaderboards) | Set on first post-test (never updated) |
| `firstPostTestTime` | First post-test time (leaderboards) | Set on first post-test (never updated) |
| `isLessonFullyCompleted` | Lesson completion flag | Set when post-test score ≥ 8 |
| `isUnlocked` | Lesson unlock status | Set when pre-test taken or previous lesson completed |

### SubtopicProgressEntity Fields
| Field | Description | When Set |
|-------|-------------|----------|
| `videoCompleted` | Video material completed | When video is watched |
| `problemCompleted` | Problem material completed | When problem is solved |
| `simulationCompleted` | Simulation material completed | When simulation is completed |

---

## 7. Visual Flow Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    LESSON PROGRESS FLOW                      │
└─────────────────────────────────────────────────────────────┘

1. INITIAL STATE
   ├─ Lesson 1: Unlocked
   └─ Lesson 2+: Locked

2. PRE-TEST
   ├─ User takes pre-test (30 min, 15 questions)
   ├─ Score recorded (first attempt only)
   ├─ preTestTaken = true
   ├─ Lesson unlocked (isUnlocked = true)
   ├─ Pre-test button greyed out (cannot retake)
   └─ First subtopic unlocked

3. SUBTOPICS
   ├─ Subtopic 1: Unlocked (after pre-test)
   │   ├─ Video → videoCompleted = true
   │   ├─ Problem → problemCompleted = true
   │   └─ Simulation → simulationCompleted = true
   │       └─ Subtopic 2 unlocked
   │
   ├─ Subtopic 2: Unlocked (after Subtopic 1 complete)
   │   ├─ Video → videoCompleted = true
   │   ├─ Problem → problemCompleted = true
   │   └─ Simulation → simulationCompleted = true
   │       └─ Subtopic 3 unlocked
   │
   └─ ... (repeat for all subtopics)

4. POST-TEST UNLOCK
   └─ All subtopics completed
       └─ Post-test button unlocked

5. POST-TEST
   ├─ User takes post-test (25 min, 15 questions)
   ├─ First attempt: firstPostTestScore/time recorded (never updated)
   ├─ Best attempt: postTestScore/time updated if better
   ├─ Score ≥ 8: isLessonFullyCompleted = true
   ├─ Next lesson unlocked
   └─ Can retake to improve score

6. NEXT LESSON
   └─ Repeat from step 2 (Pre-Test)
```

---

## 8. Key Rules & Constraints

### Pre-Test Rules
- ✅ Can only be taken **once** per lesson
- ✅ Must be taken before accessing subtopics
- ✅ Passing score: ≥ 6/15
- ✅ Score is stored but cannot be improved

### Subtopic Rules
- ✅ Must complete pre-test to unlock first subtopic
- ✅ Must complete all 3 materials (video, problem, simulation) to unlock next subtopic
- ✅ Sequential unlocking (cannot skip subtopics)

### Post-Test Rules
- ✅ Requires pre-test taken AND all subtopics completed
- ✅ Can be taken **multiple times**
- ✅ Passing score: ≥ 8/15
- ✅ Best score/time is tracked separately from first attempt
- ✅ First attempt score/time used for leaderboards
- ✅ Score ≥ 8 unlocks next lesson

### Lesson Completion
- ✅ Requires post-test score ≥ 8
- ✅ Automatically unlocks next lesson
- ✅ Marks lesson as fully completed
- ✅ Unlocks achievement badges

---

## 9. Database Tables

### lesson_progress
- Stores lesson-level progress per user
- Composite primary key: (username, lessonId)
- Tracks pre-test, post-test, completion status

### subtopic_progress
- Stores subtopic-level progress per user
- Tracks video, problem, simulation completion
- Composite primary key: (username, subtopicId)

### test_attempts
- Stores all test attempts (pre and post)
- Tracks score, time, lesson, test type
- Used for analytics and retry tracking

---

## 10. Leaderboard Integration

### First Post-Test Values
- Used for leaderboard rankings
- Recorded only on first attempt
- Never updated (fair comparison)
- Summed across all lessons for overall ranking

### Current/Best Values
- Used for user's current performance display
- Updated when better scores achieved
- Shown in profile and progress screens

---

## Summary

The lesson progress flow is a **sequential, gated system**:

1. **Pre-Test** → Unlocks lesson and first subtopic (one-time)
2. **Subtopics** → Sequential completion (video → problem → simulation)
3. **Post-Test** → Unlocked after all subtopics complete (retakable)
4. **Completion** → Unlocks next lesson (score ≥ 8)

The system ensures users progress through content in a structured manner while allowing post-test improvement and tracking both first-attempt and best performance metrics.

