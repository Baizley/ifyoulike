import React from "react";
import ReactMarkdown from "react-markdown";

function Recommendation(props) {
    return (
        <p class="recommendation"><ReactMarkdown source={props.recommendation}/></p>
    );
}

export default Recommendation;