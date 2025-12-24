<!DOCTYPE html>
<html lang="${locale.language!'en'}" dir="ltr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="robots" content="noindex, nofollow">
    <title>Avizi - ${msg("loginTitle",(realm.displayName!''))}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico">
    <link rel="stylesheet" href="${url.resourcesPath}/css/style.css">
</head>
<body>
    <div class="container">
        <div class="login-card">
            <div class="logo">
                <h1>Avizi</h1>
                <p>Discover Local Events</p>
            </div>

            <#if message?has_content>
                <div class="alert alert-${message.type}">
                    ${message.summary}
                </div>
            </#if>

            <form id="kc-form-login" action="${url.loginAction}" method="post">
                <div class="form-group">
                    <label for="username">${msg("usernameOrEmail")}</label>
                    <input id="username" name="username" value="${(login.username!'')}" type="text" autofocus autocomplete="username">
                </div>

                <div class="form-group">
                    <label for="password">${msg("password")}</label>
                    <input id="password" name="password" type="password" autocomplete="current-password">
                </div>

                <#if realm.rememberMe && !usernameHidden??>
                    <div class="form-group checkbox">
                        <label>
                            <input id="rememberMe" name="rememberMe" type="checkbox" tabindex="3"> ${msg("rememberMe")}
                        </label>
                    </div>
                </#if>

                <div class="form-group">
                    <input name="login" id="kc-login" type="submit" value="${msg("doLogIn")}">
                </div>
            </form>

            <#if realm.password && social.providers??>
                <div class="social-providers">
                    <div class="divider">
                        <span>or</span>
                    </div>
                    <#list social.providers as p>
                        <a href="${p.loginUrl}" class="social-button ${p.alias}">
                            ${p.displayName}
                        </a>
                    </#list>
                </div>
            </#if>

            <#if realm.resetPasswordAllowed>
                <div class="links">
                    <a href="${url.loginResetCredentialsUrl}">${msg("doForgotPassword")}</a>
                </div>
            </#if>
        </div>
    </div>
</body>
</html>