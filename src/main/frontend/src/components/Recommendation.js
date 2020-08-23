import React from "react";
import ReactMarkdown from "react-markdown";

function Recommendation(props) {
    return (
        <p className="recommendation"><ReactMarkdown source={props.recommendation.text}/></p>
    );
}

export default Recommendation;