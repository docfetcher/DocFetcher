#!/usr/bin/php
<?
# Expected variable substitution:
# "de" => "de/index.html",
# "en" => "en/index.html",
# ...
$sites = array(
	${languages}
);

$langs = array();

// http://www.thefutureoftheweb.com/blog/use-accept-language-header
if (isset($_SERVER['HTTP_ACCEPT_LANGUAGE'])) {
    // break up string into pieces (languages and q factors)
    preg_match_all('/([a-z]{1,8}(-[a-z]{1,8})?)\s*(;\s*q\s*=\s*(1|0\.[0-9]+))?/i', $_SERVER['HTTP_ACCEPT_LANGUAGE'], $lang_parse);

    if (count($lang_parse[1])) {
        // create a list like "en" => 0.8
        $langs = array_combine($lang_parse[1], $lang_parse[4]);
    	
        // set default to 1 for any without q factor
        foreach ($langs as $lang => $val) {
            if ($val === '') $langs[$lang] = 1;
        }

        // sort list based on value	
        arsort($langs, SORT_NUMERIC);
    }
}

foreach ($langs as $lang => $val) {
	$lang = strtolower(substr(chop($lang), 0, 2));
	if (array_key_exists($lang, $sites)) {
		header("Location: ".$sites[$lang]);
		return;
	}
}

header("Location: ".$sites["en"]);
?>
