import React from "react";
import ReactMarkdown from "react-markdown";
import { FontAwesomeIcon } from '@fortawesome/react-fontawesome';
import { faLink } from '@fortawesome/free-solid-svg-icons'


function Recommendation(props) {
    return (
        <div className="recommendation">
            <div id="header">
                <span id="score">{props.recommendation.score}</span>
                <a target="_blank" href={props.recommendation.source}>
                    <FontAwesomeIcon id="source" icon={faLink} />
                </a>
            </div>
            <ReactMarkdown source={props.recommendation.text}/>
        </div>
    );
}

export default Recommendation;