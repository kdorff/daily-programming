/**
 * http://www.reddit.com/r/dailyprogrammer/comments/2nauiv/20141124_challenge_190_easy_webscraping_sentiments/
 * 
 * Description
 * --------------------
 * Webscraping is the delicate process of gathering information from a website 
 * (usually) without the assistance of an API. Without an API, it often involves 
 * finding what ID or CLASS a certain HTML element has and then targeting it. 
 * In our latest challenge, we'll need to do this (you're free to use an API, 
 *     but, where's the fun in that!?) to find out the overall sentiment of a 
 * sample size of people.
 * We will be performing very basic sentiment analysis on a YouTube video of 
 * your choosing.
 * 
 * Task
 * --------------------
 * Your task is to scrape N (You decide but generally, the higher the sample, 
 *     the more accurate) number of comments from a YouTube video of your 
 * choice and then analyse their sentiments based on a short list of happy/sad 
 * keywords.
 * 
 * Analysis will be done by seeing how many Happy/Sad keywords are in each 
 * comment. If a comment contains more sad keywords than happy, then it can 
 * be deemed sad.
 * 
 * Here's a basic list of keywords for you to test against. I've ommited 
 * expletives to please all readers...
 * 
 * happy = ['love','loved','like','liked','awesome','amazing',
 *          'good','great','excellent']
 * sad =   ['hate','hated','dislike','disliked','awful','terrible',
 *          'bad','painful','worst']
 * 
 * Feel free to share a bigger list of keywords if you find one. A larger one 
 * would be much appreciated if you can find one.
 * 
 * Formal inputs and outputs
 * --------------------
 * 
 * Input description
 * --------------------
 * On console input, you should pass the URL of your video to be analysed.
 * 
 * Output description
 * --------------------
 * The output should consist of a statement stating something along the 
 * lines of -
 * 
 * "From a sample size of" N "Persons. This sentence is mostly" [Happy|Sad] 
 * "It contained" X "amount of Happy keywords and" X "amount of sad keywords. 
 * The general feelings towards this video were" [Happy|Sad]
 * 
 * Notes
 * --------------------
 * As pointed out by /u/pshatmsft , YouTube loads the comments via AJAX so 
 * there's a slight workaround that's been posted by /u/threeifbywhiskey .
 * 
 * Given the URL below, all you need to do is replace FullYoutubePathHere with 
 * your URL
 * 
 * https://plus.googleapis.com/u/0/_/widget/render/comments?first_party_property=YOUTUBE&href=FullYoutubePathHere
 * 
 * Remember to append your url in full 
 * (https://www.youtube.com/watch?v=dQw4w9WgXcQ as an example)
 * 
 * Hints
 * --------------------
 * The string for a Youtube comment is the following
 * <div class="CT">Youtube comment here</div>
 */

// REST URL required to fetch comments for a specific video
commentsBaseUrl = "https://plus.googleapis.com/u/0/_/widget/render/comments" +
                  "?first_party_property=YOUTUBE&href=_URL_"

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
    int commentsCount = 0
    data.findAll(commentPattern) { whole, comment ->
        commentsCount++
        parseComment(comment, allFeelings)
    }

    // Convert allFeelings to a human readable string and output
    def overallFeelings = feelingsToHuman(allFeelings)
    print   "${commentsCount} comments for ${checkUrl} are "
    println "overall ${overallFeelings}"
}

/**
 * Given a single comment HTML string, determine if it is positive
 * or negative.
 * @param comment comment in HTML format
 * @param feelings a map of feelings across all comments with int values for
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

/**
 * Convert the feelings map to a string describing the feelings.
 * @param feelings a map of feelings across all comments with int values for
 * keys positives, negatives which are the number of positive and
 * negative words found in the video.
 */
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
