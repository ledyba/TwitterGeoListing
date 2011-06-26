<html>
    <head>
        <meta name="viewport" content="initial-scale=1.0, user-scalable=no" />
        <meta name="layout" content="main" />
        <script type="text/javascript" src="http://maps.google.com/maps/api/js?sensor=false&language=ja"></script>
        <g:javascript library="load" />
        <title>ログインに失敗しました</title>
    </head>
    <body>
    <p>ログインに失敗しました。APIを使い切ってる等の可能性があります。</p>
    <p>エラーメッセージ：</p><p><g:if test="${exception != null}">
        ${exception.getMessage()}
    </g:if></p>
    <p><span style="font-size: xx-large;"><a href="${createLink(uri:'/')}">トップページへ戻る</a></span></p>
    </body>
</html>
