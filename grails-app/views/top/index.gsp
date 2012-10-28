<html>
    <head>
        <meta name="layout" content="main" />
    </head>
    <body>
    <p><span style="font-weight: bold;"><g:message code="site.title" /></span>は、Twitter上の位置情報をマップ上にマッピングすることで、住所を特定できてしまうのではないか？という素朴な疑問を実証する実験的サービスです。</p>
    <hr />
    <p><span style="font-size: xx-large;"><a href="${createLink(controller:'user', action:'login_check')}">Twitter経由でログインする！</a></span></p>
    <p>※現在、<strong>Firefox5</strong>と<strong>Chrome14開発版</strong>にて動作確認を行っております。ChromeではGoogle Mapsの表示が不安定なので、Firefoxを推奨しています。</p>
    <hr />
    <h2>説明</h2>
    <p>このアプリは、あなたのツイートを取得し、GoogleMaps上にその位置情報を表示するサービスです。</p>
    <p>OAuth認証が必要なのはAPI呼び出し回数の制限のためであり、<strong>取得する情報はすべて認証なしで誰でも取得する事ができるものです。</strong></p>
    <p>上記のリンクから認証をして使用することができます。認証完了後、Twitterから位置情報を取得し、GoogleMap上にマッピングを行います。</p>
    <hr />
    <h2>詳しい情報はこちらから</h2>
    <ul>
        <li>関連のBlog記事：<a href="http://ledyba.org/2011/06/26203210.php">Twitterで住所がバレる！？「Twitter住所特定実験」</a></li>
        <li>作者Twitterアカウント：<a href="http://twitter.com/#!/tikal">@tikal</a>（気軽にフォローしてください）</li>
    </ul>
    <hr />
    <div align="top">
        <h2>Powered by:</h2>
        <div id="powerd_by">
            <ul>
                <li><a href="http://www.grails.org/"><img src="${resource(dir:'images',file:'grails_logo.png')}" alt="Grails" align="middle"></a></li>
                <li><a href="http://www.springsource.org/"><img src="${resource(dir:'images',file:'springsource.png')}" alt="Spring Framework" align="middle"></a></li>
                <li><a href="http://code.google.com/intl/ja/apis/maps/documentation/javascript/">Google Maps API V3</a></li>
                <li><a href="http://code.google.com/p/google-maps-utility-library-v3/">google-maps-utility-library-v3</a></li>
                <li><a href="http://www.twitter.com/"><img src="${resource(dir:'images',file:'twitter_logo.png')}" alt="Twitter" align="middle"></a></li>
                <li><a href="http://twitter4j.org/ja/index.html">Twitter4j</a></li>
            </ul>
        </div>
    </div>
    </body>
</html>
