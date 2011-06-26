<html>
    <head>
        <meta name="layout" content="main" />
        <title>バックドア！</title>
    </head>
    <body>
    <p>自分以外の投稿は管理者以外取得できません。プライバシー。</p>
    <hr />
    <form action="${createLink(controller:'admin', action:'login_check')}" method="POST">
        <p>ユーザID：<input type="input" name="screenName"></p>
        <p><input type="submit" value="このユーザーでログイン" /></p>
    </form>
    </body>
</html>
