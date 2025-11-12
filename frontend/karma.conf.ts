// @ts-nocheck   

import type { Config, ConfigOptions } from 'karma';

export default function (config: Config) {
  const options: ConfigOptions = {
    basePath: '',
    frameworks: ['jasmine', '@angular-devkit/build-angular'],

    reporters: ['progress', 'kjhtml'],

    browsers: ['ChromeHeadlessNoSandbox'],

    customLaunchers: {
      ChromeHeadlessNoSandbox: {
        base: 'ChromeHeadless',
        flags: [
          '--headless=new',              
          '--no-sandbox',
          '--disable-setuid-sandbox',
          '--ignore-certificate-errors', // <-- permite HTTPS self-signed
          '--allow-insecure-localhost',  // <-- HTTPS localhost
          '--disable-web-security'
        ],
      },
    },

    singleRun: true,
    autoWatch: false,
  };

  config.set(options);
}
