import React, { useEffect, useState } from "react";

const CircularProgress = ({
  size,
  active,
  strokeWidth,
  percentage,
  color,
  textValue,
}: {
  size: number;
  active: boolean;
  strokeWidth: number;
  percentage: number;
  color: string;
  textValue: string;
}) => {
  const [progress, setProgress] = useState(0);
  useEffect(() => {
    setProgress(percentage);
  }, [percentage]);

  color = active ? "mediumpurple" : color;
  size = active ? size * 1.1 : 25;
  const viewBox = `0 0 ${size} ${size}`;
  const radius = (size - strokeWidth) / 2;
  const circumference = radius * Math.PI * 2;
  const dash = (progress * circumference) / 100;
  let bgColor = percentage == 100 ? color : "#fff";
  let textColor = percentage == 100 ? "#fff" : "ddd";
  const fontWeight = "normal";
  bgColor = active ? "#fff" : bgColor;

  let borderStroke = percentage == 100 ? color : "#eee";

  return (
    <svg width={size} height={size} viewBox={viewBox}>
      <circle
        fill={bgColor}
        stroke={borderStroke}
        cx={size / 2}
        cy={size / 2}
        r={radius}
        strokeWidth={`${strokeWidth}px`}
      />
      <circle
        fill="none"
        stroke={color}
        cx={size / 2}
        cy={size / 2}
        r={radius}
        strokeWidth={`${strokeWidth * 1.25}px`}
        transform={`rotate(-90 ${size / 2} ${size / 2})`}
        strokeDasharray={[dash, circumference - dash] as any}
        style={{ transition: "all 0.5s" }}
      />
      <text
        stroke={textColor}
        fontSize={`${size / 2}px`}
        fontWeight={fontWeight}
        x="50%"
        y="50%"
        dy={`${size / 6}px`}
        textAnchor="middle"
      >
        {`${textValue}`}
      </text>
    </svg>
  );
};

export default CircularProgress;
