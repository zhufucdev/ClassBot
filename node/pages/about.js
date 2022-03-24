import Image from "next/image";
import { Box, Typography, Card, CardHeader, CardContent, Button, CardActions, IconButton, Divider, Link } from "@mui/material";
import { mdiQqchat } from "@mdi/js"
import Icon from "@mdi/react"

import buildInfo from "../utility/build";

function About() {
    return (
        <Box>
            <Typography sx={{ marginBottom: 1 }}>关于ClassBot</Typography>
            <Card sx={{ width: '100%' }}>
                <CardHeader
                    title={<Typography variant="h5">ClassBot</Typography>}
                    subheader="致力于居家学习的Mirai机器人"
                    sx={{ display: 'flex' }}
                    avatar={<Icon path={mdiQqchat} size={2} />}/>
                <Divider />
                <CardContent>
                    <Typography variant="overline">版本号 {buildInfo.version}</Typography>
                </CardContent>
                <Divider />
                <CardActions sx={{ justifyContent: 'end' }}>
                    <Button href={buildInfo.page} variant="outlined">
                        <Typography variant="subtitle2">在GitHub上查看</Typography>
                    </Button>
                </CardActions>
            </Card>
        </Box>
    )
}

export default About