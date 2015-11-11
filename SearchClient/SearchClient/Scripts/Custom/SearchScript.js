(function () {
    var resultsPerPage = 25;
    var activePage;

    function addDocToShowlist(doc) {
        if (doc.Content == null)
            return;

        var template = '<div class="panel-heading"><b><h4 class="article-heading"></h4></b><span class="article-score"></span></div>'
            + '<div class="panel-body"><div class="article-body"></div>'
            + '<a expand="false" class="article-readmore" href="#">read more</a>'
            + '<br/><b><a class="article-link"></a></b></div>';

        var element = $('<div/>', {
            "class": "panel panel-default col-lg-10 col-lg-offset-1"
        }).html(template);

        (function (elem) {
            elem.find(".article-heading").text(doc.Number + ". " + doc.Title);
            elem.find(".article-body").text(doc.Content);
            elem.find(".article-score").text("Score: " + doc.Score);
            elem.find(".article-link").text(doc.Url).attr("href", doc.Url);
            elem.find(".article-readmore").click(function (event) {
                event.preventDefault();
                var expand = $(this).attr("expand");
                if (expand === "false") {
                    elem.find(".article-body").css("max-height", "none");
                    $(this).text("read less");
                    $(this).attr("expand", "true");

                } else {
                    elem.find(".article-body").css("max-height", "100px");
                    $(this).text("read more");
                    $(this).attr("expand", "false");
                }
            });
        })(element);


        $("#searchResults").append(element);
    }

    function showResults(data) {
        var seconds = data.TotalTime / 1000;
        $("#searchResultArea").css("visibility", "visible");
        $("#totalHitsInfo").html("<span class='badge'>" + data.TotalHits + "</span>" + " results found."
            + " Total time " + "<span class='badge'>" + seconds + "</span>" + " seconds.");
        $("#searchResults").empty();

        for (var i = 0 ; i < data.Documents.length ; i++)
            if (data.Documents[i] != null)
                addDocToShowlist(data.Documents[i]);
    }

    function setPager(totalHits, clicked) {
        var pages = totalHits / resultsPerPage;

        var elem = $("#pager .pagination");
        elem.empty();
        for (var i = 0 ; i < pages ; i++) {
            var pageLi = $("<li/>");
            pageLi.append('<a href="#">' + (i + 1) + '</a>');
            (function () {
                var pageIndex = i + 1;
                pageLi.click(function () {
                    var q = $("#queryInput").val();

                    search(q, pageIndex, resultsPerPage, function (data) {
                        showResults(data);
                        setPager(data.TotalHits, 1);
                    });
                });
            })();
            elem.append(pageLi);
        }

        activePage = clicked;
    }

    function search(query, page, resultsPerPage, callback) {
        $.get("http://localhost:8080/search", {
            query: query,
            resultsPerPage: resultsPerPage,
            pageNumber: page
        }, function (data) {
            callback(data);
        });
    }

    function hideSuggestions() {
        $("#suggestions").css("visibility", "hidden");
    }

    function setSuggestions(term, sentenceWithoutLastWord) {
        if (term.length > 3 && term !== "" && term !== " ") {
            $.get("http://localhost:8080/suggest", { term: term }, function (suggestions) {
                $("#suggestions").empty();
                $("#suggestions").css("visibility", "visible");
                for (var i = 0 ; i < suggestions.length ; i++) {
                    var suggestion = $("<div/>", {
                        "class": "suggestion",
                        text: sentenceWithoutLastWord + " " + suggestions[i]
                    });
                    (function () {
                        var sentence = sentenceWithoutLastWord;
                        var complete = suggestions[i];
                        suggestion.click(function () {
                            $("#queryInput").val(sentence + " " + complete);
                        });
                    })();
                    $("#suggestions").append(suggestion);
                }
            })
        } else
            hideSuggestions();
    }

    $(document).ready(function () {
        $("#queryInput").keyup(function (evnt) {
            var q = $("#queryInput").val();
            var lastWord;
            var spacesExist = q.indexOf(" ") != -1;
            if (spacesExist) {
                lastWord = q.substring(q.lastIndexOf(" ") + 1, q.length);
                setSuggestions(lastWord, q.substring(0, q.lastIndexOf(" ")));
            } else {
                lastWord = q;
                setSuggestions(lastWord, "");
            }

        });

        $(document).click(function () {
            hideSuggestions();
        });

        $("#SearchButton").click(function () {
            var q = $("#queryInput").val();

            search(q, 1, resultsPerPage, function (data) {
                $("#badQueryError").hide();
                showResults(data);
                setPager(data.TotalHits, 1);
                if (data.SearchReplaced) {
                    $("#badQueryError").show();
                    $("#badQueryError").empty();
                    $("#badQueryError").html("Your query is corrected. Searching for <b>" + data.SearchReplacement + "</b> instead.");
                }
            });
        });
    });
})();