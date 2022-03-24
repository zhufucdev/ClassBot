import * as React from "react";
import { AppBar as MuiAppBar, IconButton, Toolbar, Typography, Box, CssBaseline, Divider, Drawer as MuiDrawer, List, ListItem, ListItemIcon, ListItemText, ListItemButton } from '@mui/material'
import MenuIcon from '@mui/icons-material/Menu'
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft'
import ChevronRightIcon from '@mui/icons-material/ChevronRight'
import { styled, useTheme } from "@mui/material/styles"
import Link from "next/link";
import buildInfo from "../../utility/build"
import appBarItems from "../appBarItems";

const drawerWidth = 240

const openedMixin = (theme) => ({
  width: drawerWidth,
  transition: theme.transitions.create('width', {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.enteringScreen,
  }),
  overflowX: 'hidden',
});

const closedMixin = (theme) => ({
  transition: theme.transitions.create('width', {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.leavingScreen,
  }),
  overflowX: 'hidden',
  width: `calc(${theme.spacing(7)} + 1px)`,
  [theme.breakpoints.up('sm')]: {
    width: `calc(${theme.spacing(8)} + 1px)`,
  },
});

const DrawerHeader = styled('div')(({ theme }) => ({
  display: 'flex',
  alignItems: 'center',
  justifyContent: 'flex-end',
  padding: theme.spacing(0, 1),
  // necessary for content to be below app bar
  ...theme.mixins.toolbar,
}));

const Drawer = styled(MuiDrawer, { shouldForwardProp: (prop) => prop !== 'open' })(
  ({ theme, open }) => ({
    width: drawerWidth,
    flexShrink: 0,
    whiteSpace: 'nowrap',
    boxSizing: 'border-box',
    ...(open && {
      ...openedMixin(theme),
      '& .MuiDrawer-paper': openedMixin(theme),
    }),
    ...(!open && {
      ...closedMixin(theme),
      '& .MuiDrawer-paper': closedMixin(theme),
    }),
  }),
);

const AppBar = styled(MuiAppBar, {
  shouldForwardProp: (prop) => prop !== 'open',
})(({ theme, open }) => ({
  zIndex: theme.zIndex.drawer + 1,
  transition: theme.transitions.create(['width', 'margin'], {
    easing: theme.transitions.easing.sharp,
    duration: theme.transitions.duration.leavingScreen,
  }),
  ...(open && {
    marginLeft: drawerWidth,
    width: `calc(100% - ${drawerWidth}px)`,
    transition: theme.transitions.create(['width', 'margin'], {
      easing: theme.transitions.easing.sharp,
      duration: theme.transitions.duration.enteringScreen,
    }),
  }),
}));

function MyAppBar(props) {
  const theme = useTheme()
  const [state, setState] = React.useState(false)

  const toggleDrawer = (open) => (event) => {
      if (event.type === 'keydown' && (event.key === 'Tab' || event.key === 'Shift')) {
          return
      }

      setState(open)
  }
  
  return (
      <Box sx={{ display: 'flex' }}>
        <CssBaseline />
        <AppBar position="fixed" open={state}>
          <Toolbar>
            <IconButton
              size="large"
              edge="start"
              color="inherit"
              aria-label="menu" sx={{ mr: 2 }}
              onClick={toggleDrawer(true)}
              sx={{
                nargubRight: 5,
                ...(state && { display: 'none' })
              }}>
              <MenuIcon />
            </IconButton>
            <Typography variant="h6" component="div" noWrap>
              {buildInfo.name}
            </Typography>
          </Toolbar>
        </AppBar>
        <Drawer variant="permanent" open={state} onClose={toggleDrawer(false)}>
          <DrawerHeader>
            <IconButton onClick={toggleDrawer(false)}>
              {theme.direction === 'rtl' ? <ChevronRightIcon /> : <ChevronLeftIcon /> }
            </IconButton>
          </DrawerHeader>
          <Divider />
          <List>
          {appBarItems.map(
            v => <Link href={v.href} key={v.name.toLocaleLowerCase()}>
              <ListItemButton key={v.name.toLocaleLowerCase()}
                sx={{
                  minHeight: 48,
                  justifyContent: state ? 'initial' : 'center',
                  px: 2.5,
                }}>
                  <ListItemIcon sx={{
                    minWidth: 0,
                    mr: state ? 3 : 'auto',
                    justifyContent: 'center'
                  }}>
                    {v.icon}
                  </ListItemIcon>
                  <ListItemText primary={v.name} sx={{ opacity: state ? 1 : 0 }}/>
              </ListItemButton>
            </Link>
          )}
          </List>
        </Drawer>

        <Box component="main" sx={{ flexGrow: 1, p: 3 }}>
          <DrawerHeader />
          {props.children}
        </Box>
      </Box>
  )
}

export default MyAppBar
