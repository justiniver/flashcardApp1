package com.example.justiniversonflashcardapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

// MainActivity is the container for the screen that is displayed to the user
class MainActivity : ComponentActivity() {

    // When MainActivity is created, set the content to be the SimpleFlashcard
    override fun onCreate(savedInstanceState: Bundle?) {
        // Since we are overriding a function from the parent Class, we have to call the original function
        // to make sure we don't lose any important functionality.
        super.onCreate(savedInstanceState)

        // Establish the Deck that should be displayed to the user
        val deck: IDeck = TFCListDeck(tfcListAll, true)

            // Set the content for the screen to be the Flashcard app, using the deck you declared
            setContent {
                SimpleFlashcards(deck)
            }
    }
}

// Creating the SimpleFlashcard display
@Composable
fun SimpleFlashcards(deck: IDeck) {
    // These variables use 'remember' so that when they change, the screen is re-rendered
    // These are essentially the State of the program, like you used for reactConsole!
    // Determines if the card should be expanded to show the back of the card (false = front only, true = front and back)
    var expanded by remember {
        mutableStateOf(false)
    }
    // Stores the front of the current card so that we can still display it once the card is flipped
    var front by remember {
        mutableStateOf(deck.getText()?:"")
    }
    // Stores the number of attempts the user has taken, so it can be displayed when they finish the deck
    var attempts: Int by remember {
        mutableStateOf(0)
    }
    // Stores the current Deck, so we know what the current card is
    var topCard: IDeck by remember {
        mutableStateOf(deck)
    }

    // The following SimpleCard and FlashcardButtons must be within SimpleFlashcards
    // so that they have access to the above variables!

    // Creates the Card display
    @Composable
    fun SimpleCard() {
        // A value to store the formatting of text, so it does not need to be repeated
        val textModifier  = Modifier
            .padding(vertical = 70.dp)
            .fillMaxWidth()

        // Get the text for the current Card view
        // Appropriately handles the Deck once it is completed and displaying the front or back of the card
        fun cardText(getFront: Boolean = false): String {
            // If the Deck is exhausted, display the summary text
            if(topCard.getState() == DeckState.EXHAUSTED) {
                return "You've reached the end of the deck.\n" +
                        "Questions: ${topCard.getSize()}, Attempts: $attempts"
            }
            // If the card has been flipped, and we want to display the front
            // we access it from the front variable
            if(topCard.getState() == DeckState.ANSWER && getFront) {
                return front
            }
            // Otherwise, we should display the current text on the card
            return topCard.getText()?:""
        }

        // When the card is clicked, respond appropriately
        fun onClickCard() {
            // The card should only respond to a click if we are displaying the Question
            if(topCard.getState() == DeckState.QUESTION) {
                // Expand the card to show the Answer
                expanded = !expanded
                // Set the front variable so we can still display the front of the card
                front = cardText(true)
                // Flip the card so we can get the text on the back
                topCard = topCard.flip()
            }
        }

        // Below are the elements that are used to create the visuals on the screen
        // We use a card component to create the flashcard
        Card(
            shape = RoundedCornerShape(8.dp), // The card has rounded corners
            modifier = Modifier
                .fillMaxWidth()               // The card fills the width of the screen
                .clickable { onClickCard() }  // The card is clickable and when clicked, calls the onClickCard function
        ) {
            Column (
                verticalArrangement = Arrangement.Center,           // The content of the card should be aligned in the center vertically
                horizontalAlignment = Alignment.CenterHorizontally  // And horizontally
            ) {
                Text(                                               // The card displays a Text element
                    text = cardText(true),                          // The text on the card should be the front of the Card
                    textAlign = TextAlign.Center,                   // Center align the text
                    modifier = textModifier                         // Apply the appropriate formatting
                )
                AnimatedVisibility(visible = expanded) {            // When the card is expanded, show the following
                    Text(                                           // Another text element containing the back of the card
                        text = cardText(false),                     // The text should show the back of the card
                        color = Color(0xFF09A129),                  // Make the text green -- it is the answer after all!
                        textAlign = TextAlign.Center,
                        modifier = textModifier
                    )
                }
            }
        }
    }

    // The other piece of our display are the available buttons

    // Creates the button display
    @Composable
    fun FlashcardButtons() {
        // The common padding for all buttons
        val buttonModifier = Modifier.padding(20.dp)

        // When the Incorrect or Correct buttons are clicked, proceed to the next card
        fun onClickNext(correct: Boolean) {
            // When we move to the next card, it should not be expanded
            expanded = false
            // Move to the next card in the deck
            topCard = topCard.next(correct)
            // Increase the number of attempts
            attempts ++
        }

        // The buttons should be next to eachother in the same Row
        Row (modifier = Modifier.padding(10.dp))
        {
            // If we've reached the end of the deck, we should only see one button
            if(topCard.getState() == DeckState.EXHAUSTED) {
                // Create a Button with the text: "Restart"
                Button(
                    onClick = { topCard = deck },   // When the button is clicked, reset to the original deck
                    modifier = buttonModifier       // Use the standard button formatting
                )
                {
                    Text(text = "Restart")         // The button content should be the text "Restart"

                }
            } else {
                // Otherwise, we should see the Incorrect and Correct Buttons
                Button(
                    // The button should only be enabled if we have displayed the answer to the card
                    enabled = topCard.getState() == DeckState.ANSWER,
                    // When clicked, proceed to the next card.
                    // If they selected this button, they got the question wrong.
                    onClick = { onClickNext(false) },
                    // Use the standard button formatting
                    modifier = buttonModifier
                )
                {
                    Text(text = "Incorrect")    // This is the button for "Incorrect"

                }
                Button(
                    enabled = topCard.getState() == DeckState.ANSWER,
                    onClick = { onClickNext(true) },      // In this case, they got the question correct.
                    modifier = buttonModifier
                )
                {
                    Text(text = "Correct")     // This is the button for "Correct"
                }
            }
        }
    }

    // Put the complete display together.
    // Show the Card and the Buttons in one column.
    Column(
        verticalArrangement = Arrangement.Center,           // Align the column in the center of the screen vertically
        horizontalAlignment = Alignment.CenterHorizontally, // And horizontally
        modifier = Modifier
            .fillMaxHeight()                                // This column should take up the entire screen
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        SimpleCard()                                        // Display the SimpleCard
        FlashcardButtons()                                  // Display the FlashcardButtons
    }
}