package com.example.justiniversonflashcardapp

// (just useful values for
// the separation characters)
val sepCard = "|"
val sepTag = ","

// a card has a front, back, and associated tags
data class TaggedFlashCard(val front: String, val back: String, val tags: List<String>) {
    fun isTagged(tag: String): Boolean = tags.any { it == tag }

    fun fileFormat(): String = "${front}${sepCard}${back}${sepCard}${tags.joinToString(sepTag)}"
}

val qCB = "What is the capital of Cuba?"
val aCB = "Havana"

val qJP = "What is the capital of Japan?"
val aJP = "Tokyo"

val qMN = "What is the capital of Mongolia?"
val aMN = "Ulaanbaatar"

val qCmaj9 = "What are the notes in Cmaj9?"
val aCmaj9 = "CEGBD"

val tagGeo = "geography"
val tagEasy = "easy"
val tagHard = "hard"
val tagUnused = "unused"

val tfcCB = TaggedFlashCard(qCB, aCB, listOf(tagGeo))
val tfcJP = TaggedFlashCard(qJP, aJP, listOf(tagGeo, tagEasy))
val tfcMN = TaggedFlashCard(qMN, aMN, listOf(tagGeo, tagHard))
val tfcMusic = TaggedFlashCard(qCmaj9, aCmaj9, emptyList<String>())

val tfcListAll = listOf(tfcJP, tfcCB, tfcMN, tfcMusic)
val tfcListCB = listOf(tfcCB)
val tfcListEmpty: List<TaggedFlashCard> = listOf()

// takes the formatted string and produces a corresponding tagged flashcard
fun stringToTaggedFlashCard(fcString: String): TaggedFlashCard {
    val splitString = fcString.split("|")
    return TaggedFlashCard(splitString[0], splitString[1], splitString[2].split(sepTag))
}

// The deck is either exhausted,
// showing the question, or
// showing the answer
enum class DeckState {
    EXHAUSTED,
    QUESTION,
    ANSWER,
}

// Basic functionality of any deck
interface IDeck {
    // The state of the deck
    fun getState(): DeckState

    // The currently visible text
    // (or null if exhausted)
    fun getText(): String?

    // The number of question/answer pairs
    // (does not change when question are
    // cycled to the end of the deck)
    fun getSize(): Int

    // Shifts from question -> answer
    // (if not QUESTION state, returns the same IDeck)
    fun flip(): IDeck

    // Shifts from answer -> next question (or exhaustion);
    // if the current question was correct it is discarded,
    // otherwise cycled to the end of the deck
    // (if not ANSWER state, returns the same IDeck)
    fun next(correct: Boolean): IDeck
}

// takes in a list of tagged flashcards and a boolean which determines
// whether the flashcard is on the front utilizing the IDeck interface
data class TFCListDeck(val tfcList: List<TaggedFlashCard>, val isFront: Boolean) : IDeck {
    // checks if list is empty and if not checks boolean input and returns
    // appropriate deck state
    override fun getState(): DeckState {
        if (tfcList.isEmpty()) {
            return DeckState.EXHAUSTED
        } else if (isFront) {
            return DeckState.QUESTION
        } else {
            return DeckState.ANSWER
        }
    }

    // checks if list is empty and returns null is empty, and if not checks
    // boolean input and returns appropriate string
    override fun getText(): String? {
        if (tfcList.isEmpty()) {
            return null
        } else if (isFront) {
            return tfcList.first().front
        } else {
            return tfcList.first().back
        }
    }

    // returns list size as integer
    override fun getSize(): Int = tfcList.size

    // if on front of flashcard goes to back and vice versa
    override fun flip(): IDeck {
        return when (isFront) {
            true -> TFCListDeck(tfcList, !isFront)
            false -> TFCListDeck(tfcList, isFront)
        }
    }

    // drops current card and returns next card
    // if user gets question wrong, adds flashcard to back of list
    override fun next(correct: Boolean): IDeck {
        return when (correct) {
            true -> TFCListDeck(tfcList.drop(1), true)
            false -> TFCListDeck(tfcList.drop(1) + tfcList.first(), true)
        }
    }
}

// takes in a list of tagged flashcards and a boolean which determines
// whether the flashcard is on the front utilizing the IDeck interface
data class PerfectSquaresDeck(val numInput: Int, val isFront: Boolean, val sequence: List<Int>) : IDeck {
    override fun getState(): DeckState {
        if (sequence.isEmpty()) {
            return DeckState.EXHAUSTED
        } else if (isFront) {
            return DeckState.QUESTION
        } else {
            return DeckState.ANSWER
        }
    }

    override fun getText(): String? {
        if (sequence.isEmpty()) {
            return null
        } else if (isFront) {
            return "$numInput^2 = ?"
        } else {
            return "${numInput * numInput}"
        }
    }

    override fun getSize(): Int = sequence.size

    override fun flip(): IDeck {
        return when (isFront) {
            true -> PerfectSquaresDeck(numInput, !isFront, sequence)
            false -> PerfectSquaresDeck(numInput, isFront, sequence)
        }
    }

    override fun next(correct: Boolean): IDeck {
        return when (correct) {
            true -> PerfectSquaresDeck(numInput - 1, !isFront, sequence.drop(1))
            false -> PerfectSquaresDeck(numInput - 1, !isFront, sequence.drop(1) + listOf(sequence.first()))
        }
    }
}
