import { Button } from "@hilla/react-components/Button.js";
import { Notification } from "@hilla/react-components/Notification.js";
import { TextField } from "@hilla/react-components/TextField.js";
import {QAService} from "Frontend/generated/endpoints.js";
import {StreamingChatService} from "Frontend/generated/endpoints.js";
import { useState } from "react";

export default function MainView() {
  const [message, setMessage] = useState("");
  return (
    <>
      <TextField
        label="Your message"
        onValueChanged={(e) => {
          setMessage(e.detail.value);
        }}
      />
      <Button
        onClick={async () => {
          const serverResponse : any = await StreamingChatService.streamChat(message);
          Notification.show(serverResponse);
        }}
      >
        Say hello
      </Button>
    </>
  );
}
