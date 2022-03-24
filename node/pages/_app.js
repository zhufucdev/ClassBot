import React from 'react'
import { CacheProvider } from '@emotion/react'
import { ThemeProvider, CssBaseline, useMediaQuery } from '@mui/material'

/* Style */
import '../styles/globals.css'
import createEmotionCache from '../utility/createEmotionCache'
import { createTheme } from '@mui/material/styles'

/* Components */
import MyAppBar from '../styles/component/MyAppBar'

const clientSideEmotionCache = createEmotionCache()

function MyApp({ Component, pageProps, emotionCache = clientSideEmotionCache }) {
  const prefersDarkMode = useMediaQuery('(prefers-color-scheme: dark)')

  const theme = React.useMemo(
      () => createTheme({
        palette: {
          mode: prefersDarkMode ? 'dark' : 'light'
        }
      },
      [prefersDarkMode]
    )
  )

  return (
    <CacheProvider value={emotionCache}>
      <ThemeProvider theme={theme}>
        <CssBaseline />
        <MyAppBar>
          <Component {...pageProps} />
        </MyAppBar>
      </ThemeProvider>
    </CacheProvider>
  )
}

export default MyApp
