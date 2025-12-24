<!DOCTYPE html>
<html lang="${(locale!'en')}" dir="ltr">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="robots" content="noindex, nofollow">
    <title>Avizi - ${msg("registerTitle")}</title>
    <link rel="icon" href="${url.resourcesPath}/img/favicon.ico">
    <link rel="stylesheet" href="${url.resourcesPath}/css/style.css">
</head>
<body>
    <div class="container">
        <div class="login-card">
            <div class="logo">
                <h1>Avizi</h1>
                <p>Join the Event Discovery Community</p>
            </div>

            <#if message?has_content>
                <div class="alert alert-${message.type}">
                    ${message.summary}
                </div>
            </#if>

            <form id="kc-register-form" action="${url.registrationAction}" method="post">
                <div class="form-group">
                    <label for="firstName">${msg("firstName")}</label>
                    <input type="text" id="firstName" name="firstName" value="${(register.formData.firstName!'')}" autocomplete="given-name">
                </div>

                <div class="form-group">
                    <label for="lastName">${msg("lastName")}</label>
                    <input type="text" id="lastName" name="lastName" value="${(register.formData.lastName!'')}" autocomplete="family-name">
                </div>

                <div class="form-group">
                    <label for="email">${msg("email")}</label>
                    <input type="email" id="email" name="email" value="${(register.formData.email!'')}" autocomplete="email">
                </div>

                <#if !realm.registrationEmailAsUsername>
                    <div class="form-group">
                        <label for="username">${msg("username")}</label>
                        <input type="text" id="username" name="username" value="${(register.formData.username!'')}" autocomplete="username">
                    </div>
                </#if>

                <#if passwordRequired??>
                    <div class="form-group">
                        <label for="password">${msg("password")}</label>
                        <input type="password" id="password" name="password" autocomplete="new-password">
                    </div>

                    <div class="form-group">
                        <label for="password-confirm">${msg("passwordConfirm")}</label>
                        <input type="password" id="password-confirm" name="password-confirm" autocomplete="new-password">
                    </div>
                </#if>

                <#if recaptchaRequired??>
                    <div class="form-group">
                        <div class="g-recaptcha" data-size="compact" data-sitekey="${recaptchaSiteKey}"></div>
                    </div>
                </#if>

                <div class="form-group">
                    <input type="submit" value="${msg("doRegister")}" class="btn btn-primary">
                </div>
            </form>

            <div class="links">
                <a href="${url.loginUrl}">${msg("backToLogin")}</a>
            </div>
        </div>
    </div>
</body>
</html>