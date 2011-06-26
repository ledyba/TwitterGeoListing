<html>
    <head>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
        <meta name="layout" content="main" />
        <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&language=ja"></script>
        <title>ログインに失敗しました</title>
    </head>
    <body>
    <p>ログインに失敗しました。</p>
    <p>エラーメッセージ：</p><p><g:if test="${exception != null}">
        ${exception.getLocalizedMessage()}
    </g:if></p>
    <p><span style="font-size: xx-large;"><a href="${createLink(action:'index')}">バックドアへ戻る</a></span></p>
    </body>
</html>
