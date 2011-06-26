<html xmlns:g="http://www.w3.org/1999/XSL/Transform">
<head>
    <meta name="viewport" content="initial-scale=1.0, user-scalable=no"/>
    <meta name="layout" content="main"/>
    <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&language=ja"></script>
    <title>ユーザー「@${userInstance.screenName}」の位置情報を取得</title>
    <g:javascript library="markerclusterer_compiled"/>
    <g:javascript library="load"/>
</head>
<body>
<p>次のユーザーの位置情報をリストアップします。</p>

<div style="display: inline;">
    <g:if test="${faceImageUrl != null && faceImageUrl.length() > 0}">
        <a href="http://www.twitter.com/${userInstance.screenName.encodeAsHTML()}/" target="_blank">
            <img src="${faceImageUrl}" width="48px" height="48px" alt="${userInstance.screenName.encodeAsHTML()}" align="middle"/>
        </a>
    </g:if>
        <a href="http://www.twitter.com/${userInstance.screenName.encodeAsHTML()}/" target="_blank">${userInstance.screenName.encodeAsHTML()}</a>
</div>
<div style="margin: 5px;" style="display: inline;">
    <input type="button" id="load_btn"
           style="margin: 5px 10px; padding-left: 20px; padding-right: 20px; font-weight: bold; font-size: x-large; border-width: 2px; display: inline;" value="ロード開始"/>
    <img src="${resource(dir:'images',file:'spinner.gif')}" id="spinner" style="display: none;" width="16px" height="16px" align="middle"/>
</div>
<hr/>
<div id="twitter_info" style="display: none;"></div>
<div id="server_info" style="display: none;"></div>
<div id="map_box" style="display: none;">
    <div id="google_map_box" style="height: 500px; width: 100%;"></div>
    <hr />
    <h2>住所が分かるみたい…どうすれば？</h2>
    <p>このアプリでは自分の位置情報しか取得できないようになっていますが、<strong>この情報自体は任意の第三者が取得することができます。</strong>（実際、私（開発者）専用の誰の情報でも取得できるモードがつくってあります）</p>
    <p>あなたが望めば、<a href="http://twitter.com/settings/account"  target="_blank">Twitterの設定画面</a>から、あなたの位置情報をすべて消すことができます。</p>
    <a href="http://twitter.com/settings/account" target="_blank"><img src="${resource(dir:'images',file:'twitter-acount-settings.jpg')}" alt="設定画面の解説"></a>
    <p>書かれている通り、大体30分くらい時間が必要なようです。</p>
</div>
</body>
</html>
