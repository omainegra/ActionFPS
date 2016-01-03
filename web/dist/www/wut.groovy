import org.jsoup.Jsoup

def js = Jsoup.parse(new File("web/dist/www/template.html"), "UTF-8")

if ( title != nil ) {
    js.title(title)
}
if ( supportsJson ) {
    js.select("#content").attr("data-has-json", "has-json")
}

js.select("#content").html("SPLITHERE")

if ( af_name != null && af_id != null ) {
    js.select("#log-in").val(af_name)
    // todo fix security
    js.select("#log-in").attr("href", "/player/?id=" + af_id);
    js.select("#download-ac-button").remove()
}

def parts = js.toString().split("SPLITHERE")
yieldUnescaped parts[0]
bodyContents()
yieldUnescaped parts[1]
