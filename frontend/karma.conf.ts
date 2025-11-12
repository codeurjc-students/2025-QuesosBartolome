// @ts-nocheck
import type { Config } from 'karma';

export default function (config: Config) {
  config.set({
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
          '--ignore-certificate-errors',
          '--allow-insecure-localhost',
          '--disable-web-security'
        ],
      },
    },

    client: {
      jasmine: {},
      clearContext: false
    },

    singleRun: true,
    autoWatch: false,
  });
}
