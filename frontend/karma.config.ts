// @ts-nocheck
module.exports = function (config) {
  config.set({
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],

    reporters: ['progress', 'kjhtml'],

    browsers: ['ChromeHeadlessNoSandbox'],

    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: [
          '--no-sandbox',
          '--disable-setuid-sandbox',
          '--ignore-certificate-errors',  // ✅ Permite HTTPS local con certificado inseguro
          '--allow-insecure-localhost',   // ✅ Permite localhost HTTPS
          '--disable-web-security'
        ]
      }
    },

    autoWatch: false,
    singleRun: true,
  });
};
