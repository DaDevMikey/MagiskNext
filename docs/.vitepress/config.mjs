import { defineConfig } from 'vitepress'

export default defineConfig({
  base: '/',
  title: "Magisk Next",
  description: "The Next Generation of Root",
  ignoreDeadLinks: true,
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Install', link: '/install' },
      { text: 'OTA', link: '/ota' },
      { text: 'Developers', link: '/developers/' },
      { text: 'Changelog', link: '/changes' }
    ],

    sidebar: [
      {
        text: 'Getting Started',
        items: [
          { text: 'Introduction', link: '/guide/' },
          { text: 'Installation', link: '/install' },
          { text: 'OTA Survival', link: '/ota' },
          { text: 'FAQ', link: '/faq' }
        ]
      },
      {
        text: 'User Docs',
        items: [
          { text: 'Guides', link: '/guides' },
          { text: 'Tools', link: '/tools' },
          { text: 'Boot Details', link: '/boot' },
          { text: 'App Changes', link: '/app_changes' }
        ]
      },
      {
        text: 'Module Developers',
        items: [
          { text: 'Module Hub Integration', link: '/developers/' },
          { text: 'Build & Development', link: '/build' },
          { text: 'Internal Details', link: '/details' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/DaDevMikey/MagiskNext' }
    ]
  }
})
