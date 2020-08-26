import React from "react";
import ReactMarkdown from "react-markdown";

function Recommendation(props) {
    return (
        <p className="recommendation">
            <span class="score">{props.recommendation.score}</span>
            <a href={props.recommendation.source}><i class="material-icons source">link</i></a>
            <ReactMarkdown source={props.recommendation.text}/>
        </p>
    );
}

export default Recommendation;