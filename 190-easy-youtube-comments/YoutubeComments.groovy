// REST URL required to fetch comments for a specific video
commentsBaseUrl = "https://plus.googleapis.com/u/0/_/widget/render/comments?first_party_property=YOUTUBE&href=_URL_"
// Videos to check comments for
inputs = [
    'https://www.youtube.com/watch?v=dQw4w9WgXcQ',
    'https://www.youtube.com/watch?v=Sllv8IPa9eo',
    'https://www.youtube.com/watch?v=7PCkvCPvDXk',
    'https://www.youtube.com/watch?v=RoNH0ITMYHQ',
    'https://www.youtube.com/watch?v=_UTWDpgig_M',
    'https://www.youtube.com/watch?v=7FC4qRD1vn8',
    'https://www.youtube.com/watch?v=pZHot7V2MkM',
    'https://www.youtube.com/watch?v=nRmMkiTB_uE',
]

// Pattern to find a comment within the comments HTML
commentPattern = ~/<div class="ct">(.*?)<\/div>/
keepCharsPattern = ~/[a-z]/

// Positive and negative words should not be larger versions of other words,
// so include 'love' but not 'loved'.
positiveWords = [
    'love', 'like', 'awesome', 'smart', 'amazing', 'good', 'great', 
    'excellent']
negativeWords = [
    'hate', 'dislike', 'awful', 'dumb', 'terrible', 'bad', 'painful', 'worst',
    'moron', 'troll']

// Kick off execution
inputs.each { checkUrl ->
    def url = commentsBaseUrl.replaceAll("_URL_", checkUrl)
    def data = new URL(url).text.toLowerCase()

    def allFeelings = [positives: 0, negatives: 0]
    data.findAll(commentPattern) { whole, comment ->
        parseComment(comment, allFeelings)
    }

    // Convert allFeelings to a human readable string and output
    def overallFeelings = feelingsToHuman(allFeelings)
    println "Comments for ${checkUrl} are overall ${overallFeelings}"
    println allFeelings
}

/**
 * Given a single comment HTML string, determine if it is positive
 * or negative.
 * @param comment comment in HTML format
 * @param map of feelings across all comments with int values for
 * keys positives, negatives which are the number of positive and
 * negative words found in the video.
 */
def parseComment(comment, feelings) {
    positiveWords.each { word ->
        comment.findAll(word) { match ->
            feelings.positives++
        }
    }
    negativeWords.each { word ->
        comment.findAll(word) { match ->
            feelings.negatives++
        }
    }
    feelings
}

def feelingsToHuman(feelings) {
    int delta = feelings.positives - feelings.negatives
    if (delta == 0) {
        "equal positive and negative"
    } else if (delta) {
        "positive"
    } else {
        "negative"
    }
}
