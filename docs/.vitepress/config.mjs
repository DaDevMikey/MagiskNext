import { defineConfig } from 'vitepress'

export default defineConfig({
  base: '/MagiskNext/',
  title: "Magisk Next",
  description: "The Next Generation of Root",
  ignoreDeadLinks: true,
  themeConfig: {
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Guide', link: '/guide/' },
      { text: 'Developers', link: '/developers/' }
    ],

    sidebar: [
      {
        text: 'Getting Started',
        items: [
          { text: 'Introduction', link: '/guide/' },
          { text: 'Installation', link: '/guide/installation' }
        ]
      },
      {
        text: 'Module Developers',
        items: [
          { text: 'Module Hub Integration', link: '/developers/' },
          { text: 'WebUI Bridge', link: '/developers/webui' },
          { text: 'Module Banners', link: '/developers/banners' },
          { text: 'Action Scripts', link: '/developers/action-scripts' }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/DaDevMikey/MagiskNext' }
    ]
  }
})
